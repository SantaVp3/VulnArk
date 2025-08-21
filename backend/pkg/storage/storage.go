package storage

import (
	"errors"
	"fmt"
	"io"
	"mime/multipart"
	"net/http"
	"os"
	"path/filepath"
	"strings"
	"time"

	"vulnark/internal/config"
	"vulnark/pkg/utils"
)

// StorageService 存储服务接口
type StorageService interface {
	UploadFile(file *multipart.FileHeader, folder string) (*FileInfo, error)
	UploadFromReader(reader io.Reader, filename, folder string, size int64) (*FileInfo, error)
	DeleteFile(filePath string) error
	GetFileURL(filePath string) string
	GetFileInfo(filePath string) (*FileInfo, error)
	GeneratePresignedURL(filePath string, expiration time.Duration) (string, error)
}

// FileInfo 文件信息
type FileInfo struct {
	Filename     string `json:"filename"`
	OriginalName string `json:"original_name"`
	FilePath     string `json:"file_path"`
	FileSize     int64  `json:"file_size"`
	FileType     string `json:"file_type"`
	MimeType     string `json:"mime_type"`
	DownloadURL  string `json:"download_url"`
}

// LocalStorageService 本地存储服务实现
type LocalStorageService struct {
	basePath string
	baseURL  string
}

// NewLocalStorageService 创建本地存储服务
func NewLocalStorageService() StorageService {
	cfg := config.AppConfig.Storage
	return &LocalStorageService{
		basePath: cfg.LocalPath,
		baseURL:  cfg.BaseURL,
	}
}

// validateFileContent 验证文件内容，防止恶意文件上传
func validateFileContent(reader io.Reader, allowedMimeTypes []string) (bool, error) {
	// 读取文件头部用于检测MIME类型
	buffer := make([]byte, 512)
	n, err := reader.Read(buffer)
	if err != nil && err != io.EOF {
		return false, err
	}

	// 检测文件的实际MIME类型
	contentType := http.DetectContentType(buffer[:n])

	// 如果没有指定允许的类型，则允许所有类型
	if len(allowedMimeTypes) == 0 {
		return true, nil
	}

	// 检查MIME类型是否在允许列表中
	for _, allowed := range allowedMimeTypes {
		if strings.HasPrefix(contentType, allowed) {
			return true, nil
		}
	}

	return false, fmt.Errorf("不支持的文件类型: %s", contentType)
}

// sanitizeFilename 清理文件名，防止路径遍历
func sanitizeFilename(filename string) (string, error) {
	// 只保留文件名部分，去除路径
	cleanName := filepath.Base(filename)

	// 检查是否包含危险字符
	if strings.Contains(cleanName, "..") || strings.ContainsAny(cleanName, "/\\") {
		return "", errors.New("文件名包含非法字符")
	}

	// 检查文件名长度
	if len(cleanName) > 255 {
		return "", errors.New("文件名过长")
	}

	// 检查是否为空
	if cleanName == "" || cleanName == "." {
		return "", errors.New("无效的文件名")
	}

	return cleanName, nil
}

// UploadFile 上传文件
func (s *LocalStorageService) UploadFile(file *multipart.FileHeader, folder string) (*FileInfo, error) {
	// 验证文件名
	cleanFilename, err := sanitizeFilename(file.Filename)
	if err != nil {
		return nil, fmt.Errorf("文件名验证失败: %v", err)
	}

	// 打开上传的文件
	src, err := file.Open()
	if err != nil {
		return nil, fmt.Errorf("打开文件失败: %v", err)
	}
	defer src.Close()

	return s.UploadFromReader(src, cleanFilename, folder, file.Size)
}

// sanitizeFolderPath 清理和验证文件夹路径
func sanitizeFolderPath(folder string) (string, error) {
	// 清理路径
	cleaned := filepath.Clean(folder)

	// 检查是否包含路径遍历
	if strings.Contains(cleaned, "..") {
		return "", errors.New("路径包含非法字符")
	}

	// 确保是相对路径
	if filepath.IsAbs(cleaned) {
		return "", errors.New("不允许绝对路径")
	}

	// 限制在允许的目录内
	allowedFolders := []string{"reports", "attachments", "avatars", "temp", "uploads"}
	for _, allowed := range allowedFolders {
		if strings.HasPrefix(cleaned, allowed) || cleaned == allowed {
			return cleaned, nil
		}
	}

	return "", errors.New("不允许的目录")
}

// UploadFromReader 从Reader上传文件
func (s *LocalStorageService) UploadFromReader(reader io.Reader, filename, folder string, size int64) (*FileInfo, error) {
	// 验证文件夹路径
	cleanFolder, err := sanitizeFolderPath(folder)
	if err != nil {
		return nil, fmt.Errorf("无效的文件夹路径: %v", err)
	}

	// 验证文件名
	cleanFilename, err := sanitizeFilename(filename)
	if err != nil {
		return nil, fmt.Errorf("文件名验证失败: %v", err)
	}

	// 生成唯一文件名
	ext := filepath.Ext(cleanFilename)
	uniqueName := fmt.Sprintf("%d_%s%s", time.Now().UnixNano(), utils.SHA256Hash(cleanFilename)[:8], ext)

	// 构建文件路径
	relativePath := filepath.Join(cleanFolder, time.Now().Format("2006/01/02"), uniqueName)
	fullPath := filepath.Join(s.basePath, relativePath)
	
	// 创建目录
	if err := os.MkdirAll(filepath.Dir(fullPath), 0755); err != nil {
		return nil, fmt.Errorf("创建目录失败: %v", err)
	}
	
	// 创建目标文件
	dst, err := os.Create(fullPath)
	if err != nil {
		return nil, fmt.Errorf("创建文件失败: %v", err)
	}
	defer dst.Close()
	
	// 复制文件内容
	written, err := io.Copy(dst, reader)
	if err != nil {
		return nil, fmt.Errorf("写入文件失败: %v", err)
	}
	
	// 获取文件类型
	fileType := s.getFileType(ext)
	mimeType := s.getMimeType(ext)
	
	// 生成下载URL
	downloadURL := s.GetFileURL(relativePath)
	
	return &FileInfo{
		Filename:     uniqueName,
		OriginalName: filename,
		FilePath:     relativePath,
		FileSize:     written,
		FileType:     fileType,
		MimeType:     mimeType,
		DownloadURL:  downloadURL,
	}, nil
}

// validateFilePath 验证文件路径，防止路径遍历
func (s *LocalStorageService) validateFilePath(filePath string) (string, error) {
	// 清理路径
	cleanPath := filepath.Clean(filePath)

	// 检查路径遍历
	if strings.Contains(cleanPath, "..") {
		return "", errors.New("无效的文件路径")
	}

	// 确保文件在允许的目录内
	fullPath := filepath.Join(s.basePath, cleanPath)

	// 验证文件确实在basePath内
	absBasePath, err := filepath.Abs(s.basePath)
	if err != nil {
		return "", fmt.Errorf("获取基础路径失败: %v", err)
	}

	absFullPath, err := filepath.Abs(fullPath)
	if err != nil {
		return "", fmt.Errorf("获取文件路径失败: %v", err)
	}

	if !strings.HasPrefix(absFullPath, absBasePath) {
		return "", errors.New("文件路径超出允许范围")
	}

	return fullPath, nil
}

// DeleteFile 删除文件
func (s *LocalStorageService) DeleteFile(filePath string) error {
	validPath, err := s.validateFilePath(filePath)
	if err != nil {
		return err
	}

	return os.Remove(validPath)
}

// GetFileURL 获取文件URL
func (s *LocalStorageService) GetFileURL(filePath string) string {
	return fmt.Sprintf("%s/%s", strings.TrimRight(s.baseURL, "/"), strings.TrimLeft(filePath, "/"))
}

// GetFileInfo 获取文件信息
func (s *LocalStorageService) GetFileInfo(filePath string) (*FileInfo, error) {
	validPath, err := s.validateFilePath(filePath)
	if err != nil {
		return nil, err
	}

	stat, err := os.Stat(validPath)
	if err != nil {
		return nil, fmt.Errorf("获取文件信息失败: %v", err)
	}

	ext := filepath.Ext(filePath)
	filename := filepath.Base(filePath)

	return &FileInfo{
		Filename:     filename,
		OriginalName: filename,
		FilePath:     filePath,
		FileSize:     stat.Size(),
		FileType:     s.getFileType(ext),
		MimeType:     s.getMimeType(ext),
		DownloadURL:  s.GetFileURL(filePath),
	}, nil
}

// GeneratePresignedURL 生成预签名URL（本地存储直接返回普通URL）
func (s *LocalStorageService) GeneratePresignedURL(filePath string, expiration time.Duration) (string, error) {
	return s.GetFileURL(filePath), nil
}

// getFileType 根据扩展名获取文件类型
func (s *LocalStorageService) getFileType(ext string) string {
	ext = strings.ToLower(ext)
	switch ext {
	case ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp":
		return "image"
	case ".pdf":
		return "pdf"
	case ".doc", ".docx":
		return "document"
	case ".xls", ".xlsx":
		return "spreadsheet"
	case ".ppt", ".pptx":
		return "presentation"
	case ".txt", ".md":
		return "text"
	case ".zip", ".rar", ".7z":
		return "archive"
	case ".mp4", ".avi", ".mov", ".wmv":
		return "video"
	case ".mp3", ".wav", ".flac":
		return "audio"
	default:
		return "other"
	}
}

// getMimeType 根据扩展名获取MIME类型
func (s *LocalStorageService) getMimeType(ext string) string {
	ext = strings.ToLower(ext)
	switch ext {
	case ".jpg", ".jpeg":
		return "image/jpeg"
	case ".png":
		return "image/png"
	case ".gif":
		return "image/gif"
	case ".pdf":
		return "application/pdf"
	case ".doc":
		return "application/msword"
	case ".docx":
		return "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
	case ".xls":
		return "application/vnd.ms-excel"
	case ".xlsx":
		return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
	case ".txt":
		return "text/plain"
	case ".html":
		return "text/html"
	case ".json":
		return "application/json"
	case ".zip":
		return "application/zip"
	default:
		return "application/octet-stream"
	}
}

// NewStorageService 创建存储服务（工厂方法）
func NewStorageService() StorageService {
	// 目前只实现本地存储，后续可以扩展OSS、S3等
	return NewLocalStorageService()
}

// ValidateFileType 验证文件类型
func ValidateFileType(filename string, allowedTypes []string) bool {
	if len(allowedTypes) == 0 {
		return true // 如果没有限制，则允许所有类型
	}

	ext := strings.ToLower(filepath.Ext(filename))
	for _, allowedType := range allowedTypes {
		if ext == strings.ToLower(allowedType) {
			return true
		}
	}
	return false
}

// ValidateFileSize 验证文件大小
func ValidateFileSize(size int64, maxSize int64) bool {
	if maxSize <= 0 {
		return true // 如果没有限制，则允许任意大小
	}
	return size <= maxSize
}

// GetAllowedReportTypes 获取允许的报告文件类型
func GetAllowedReportTypes() []string {
	return []string{".pdf", ".doc", ".docx"}
}

// GetMaxReportSize 获取报告文件最大大小（100MB）
func GetMaxReportSize() int64 {
	return 100 * 1024 * 1024 // 100MB
}
