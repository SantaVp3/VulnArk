package utils

import (
	"encoding/json"
	"strconv"
	"strings"
	"time"
)

// ParseUintSlice 解析逗号分隔的uint切片
func ParseUintSlice(str, sep string) ([]uint, error) {
	if str == "" {
		return []uint{}, nil
	}
	
	parts := strings.Split(str, sep)
	result := make([]uint, 0, len(parts))
	
	for _, part := range parts {
		part = strings.TrimSpace(part)
		if part == "" {
			continue
		}
		
		val, err := strconv.ParseUint(part, 10, 32)
		if err != nil {
			return nil, err
		}
		result = append(result, uint(val))
	}
	
	return result, nil
}

// ParseStringSlice 解析逗号分隔的字符串切片
func ParseStringSlice(str, sep string) []string {
	if str == "" {
		return []string{}
	}
	
	parts := strings.Split(str, sep)
	result := make([]string, 0, len(parts))
	
	for _, part := range parts {
		part = strings.TrimSpace(part)
		if part != "" {
			result = append(result, part)
		}
	}
	
	return result
}

// ParseDate 解析日期字符串（YYYY-MM-DD格式）
func ParseDate(dateStr string) (time.Time, error) {
	return time.Parse("2006-01-02", dateStr)
}

// ParseDateTime 解析日期时间字符串（YYYY-MM-DD HH:MM:SS格式）
func ParseDateTime(dateTimeStr string) (time.Time, error) {
	return time.Parse("2006-01-02 15:04:05", dateTimeStr)
}

// FormatFileSize 格式化文件大小
func FormatFileSize(size int64) string {
	const unit = 1024
	if size < unit {
		return strconv.FormatInt(size, 10) + " B"
	}
	
	div, exp := int64(unit), 0
	for n := size / unit; n >= unit; n /= unit {
		div *= unit
		exp++
	}
	
	units := []string{"KB", "MB", "GB", "TB", "PB"}
	return strconv.FormatFloat(float64(size)/float64(div), 'f', 1, 64) + " " + units[exp]
}

// JoinUints 将uint切片连接为字符串
func JoinUints(uints []uint, sep string) string {
	if len(uints) == 0 {
		return ""
	}
	
	strs := make([]string, len(uints))
	for i, u := range uints {
		strs[i] = strconv.FormatUint(uint64(u), 10)
	}
	
	return strings.Join(strs, sep)
}

// Contains 检查字符串切片是否包含指定字符串
func Contains(slice []string, item string) bool {
	for _, s := range slice {
		if s == item {
			return true
		}
	}
	return false
}

// ContainsUint 检查uint切片是否包含指定uint
func ContainsUint(slice []uint, item uint) bool {
	for _, u := range slice {
		if u == item {
			return true
		}
	}
	return false
}

// RemoveDuplicates 去除字符串切片中的重复项
func RemoveDuplicates(slice []string) []string {
	keys := make(map[string]bool)
	result := []string{}
	
	for _, item := range slice {
		if !keys[item] {
			keys[item] = true
			result = append(result, item)
		}
	}
	
	return result
}

// RemoveDuplicatesUint 去除uint切片中的重复项
func RemoveDuplicatesUint(slice []uint) []uint {
	keys := make(map[uint]bool)
	result := []uint{}
	
	for _, item := range slice {
		if !keys[item] {
			keys[item] = true
			result = append(result, item)
		}
	}
	
	return result
}

// ToJSON 将对象转换为JSON字节数组
func ToJSON(v interface{}) ([]byte, error) {
	return json.Marshal(v)
}
