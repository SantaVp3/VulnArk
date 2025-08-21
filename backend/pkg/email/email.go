package email

import (
	"bytes"
	"fmt"
	"html/template"
	"net/smtp"
	"strconv"
	"strings"
	"time"

	"vulnark/internal/config"
)

// EmailService 邮件服务接口
type EmailService interface {
	SendEmail(to []string, cc []string, bcc []string, subject, content string) error
	SendTemplateEmail(to []string, cc []string, bcc []string, subject, templateContent string, variables map[string]interface{}) error
	ValidateEmailAddress(email string) bool
	TestConnection() error
}

// SMTPEmailService SMTP邮件服务实现
type SMTPEmailService struct {
	host     string
	port     string
	username string
	password string
	from     string
}

// NewSMTPEmailService 创建SMTP邮件服务
func NewSMTPEmailService() EmailService {
	cfg := config.AppConfig.Email
	return &SMTPEmailService{
		host:     cfg.Host,
		port:     strconv.Itoa(cfg.Port),
		username: cfg.Username,
		password: cfg.Password,
		from:     cfg.From,
	}
}

// SendEmail 发送邮件
func (s *SMTPEmailService) SendEmail(to []string, cc []string, bcc []string, subject, content string) error {
	// 构建邮件头
	headers := make(map[string]string)
	headers["From"] = s.from
	headers["To"] = strings.Join(to, ",")
	if len(cc) > 0 {
		headers["Cc"] = strings.Join(cc, ",")
	}
	headers["Subject"] = subject
	headers["MIME-Version"] = "1.0"
	headers["Content-Type"] = "text/html; charset=UTF-8"
	headers["Date"] = time.Now().Format(time.RFC1123Z)

	// 构建邮件内容
	message := ""
	for k, v := range headers {
		message += fmt.Sprintf("%s: %s\r\n", k, v)
	}
	message += "\r\n" + content

	// 合并所有收件人
	allRecipients := append(to, cc...)
	allRecipients = append(allRecipients, bcc...)

	// 发送邮件
	auth := smtp.PlainAuth("", s.username, s.password, s.host)
	addr := fmt.Sprintf("%s:%s", s.host, s.port)
	
	return smtp.SendMail(addr, auth, s.from, allRecipients, []byte(message))
}

// SendTemplateEmail 发送模板邮件
func (s *SMTPEmailService) SendTemplateEmail(to []string, cc []string, bcc []string, subject, templateContent string, variables map[string]interface{}) error {
	// 解析模板
	tmpl, err := template.New("email").Parse(templateContent)
	if err != nil {
		return fmt.Errorf("解析邮件模板失败: %v", err)
	}

	// 渲染模板
	var buf bytes.Buffer
	if err := tmpl.Execute(&buf, variables); err != nil {
		return fmt.Errorf("渲染邮件模板失败: %v", err)
	}

	// 发送邮件
	return s.SendEmail(to, cc, bcc, subject, buf.String())
}

// ValidateEmailAddress 验证邮箱地址
func (s *SMTPEmailService) ValidateEmailAddress(email string) bool {
	// 简单的邮箱格式验证
	if !strings.Contains(email, "@") {
		return false
	}
	
	parts := strings.Split(email, "@")
	if len(parts) != 2 {
		return false
	}
	
	if len(parts[0]) == 0 || len(parts[1]) == 0 {
		return false
	}
	
	if !strings.Contains(parts[1], ".") {
		return false
	}
	
	return true
}

// TestConnection 测试邮件服务器连接
func (s *SMTPEmailService) TestConnection() error {
	auth := smtp.PlainAuth("", s.username, s.password, s.host)
	addr := fmt.Sprintf("%s:%s", s.host, s.port)
	
	// 尝试连接并认证
	client, err := smtp.Dial(addr)
	if err != nil {
		return fmt.Errorf("连接邮件服务器失败: %v", err)
	}
	defer client.Close()

	if err := client.Auth(auth); err != nil {
		return fmt.Errorf("邮件服务器认证失败: %v", err)
	}

	return nil
}

// MockEmailService 模拟邮件服务（用于测试）
type MockEmailService struct {
	emails []MockEmail
}

// MockEmail 模拟邮件
type MockEmail struct {
	To      []string
	Cc      []string
	Bcc     []string
	Subject string
	Content string
	SentAt  time.Time
}

// NewMockEmailService 创建模拟邮件服务
func NewMockEmailService() *MockEmailService {
	return &MockEmailService{
		emails: make([]MockEmail, 0),
	}
}

// SendEmail 发送邮件（模拟）
func (m *MockEmailService) SendEmail(to []string, cc []string, bcc []string, subject, content string) error {
	email := MockEmail{
		To:      to,
		Cc:      cc,
		Bcc:     bcc,
		Subject: subject,
		Content: content,
		SentAt:  time.Now(),
	}
	m.emails = append(m.emails, email)
	return nil
}

// SendTemplateEmail 发送模板邮件（模拟）
func (m *MockEmailService) SendTemplateEmail(to []string, cc []string, bcc []string, subject, templateContent string, variables map[string]interface{}) error {
	// 简单的模板变量替换
	content := templateContent
	for key, value := range variables {
		placeholder := fmt.Sprintf("{{.%s}}", key)
		content = strings.ReplaceAll(content, placeholder, fmt.Sprintf("%v", value))
	}
	
	return m.SendEmail(to, cc, bcc, subject, content)
}

// ValidateEmailAddress 验证邮箱地址（模拟）
func (m *MockEmailService) ValidateEmailAddress(email string) bool {
	return strings.Contains(email, "@") && strings.Contains(email, ".")
}

// TestConnection 测试连接（模拟）
func (m *MockEmailService) TestConnection() error {
	return nil
}

// GetSentEmails 获取已发送的邮件（仅用于测试）
func (m *MockEmailService) GetSentEmails() []MockEmail {
	return m.emails
}

// ClearEmails 清空邮件记录（仅用于测试）
func (m *MockEmailService) ClearEmails() {
	m.emails = make([]MockEmail, 0)
}

// NewEmailService 创建邮件服务（工厂方法）
func NewEmailService() EmailService {
	cfg := config.AppConfig.Email

	// 如果是开发环境或测试环境，使用模拟邮件服务
	if cfg.Host == "" || cfg.Host == "localhost" {
		return NewMockEmailService()
	}

	return NewSMTPEmailService()
}

// EmailTemplate 邮件模板结构
type EmailTemplate struct {
	Subject string
	Content string
}

// GetDefaultTemplates 获取默认邮件模板
func GetDefaultTemplates() map[string]EmailTemplate {
	return map[string]EmailTemplate{
		"vulnerability_assigned": {
			Subject: "漏洞分配通知 - {{.VulnerabilityTitle}}",
			Content: `
<html>
<body>
<h2>漏洞分配通知</h2>
<p>您好 {{.AssigneeName}}，</p>
<p>有一个新的漏洞已分配给您处理：</p>
<ul>
<li><strong>漏洞标题：</strong>{{.VulnerabilityTitle}}</li>
<li><strong>严重程度：</strong>{{.Severity}}</li>
<li><strong>发现时间：</strong>{{.DiscoveredAt}}</li>
<li><strong>分配人：</strong>{{.AssignerName}}</li>
</ul>
<p>请及时登录系统查看详情并处理。</p>
<p>系统地址：<a href="{{.SystemURL}}">{{.SystemURL}}</a></p>
<br>
<p>此邮件由系统自动发送，请勿回复。</p>
</body>
</html>`,
		},
		"report_submitted": {
			Subject: "报告提交通知 - {{.ReportTitle}}",
			Content: `
<html>
<body>
<h2>报告提交通知</h2>
<p>您好，</p>
<p>有一个新的报告已提交，等待您的审核：</p>
<ul>
<li><strong>报告标题：</strong>{{.ReportTitle}}</li>
<li><strong>报告类型：</strong>{{.ReportType}}</li>
<li><strong>严重程度：</strong>{{.Severity}}</li>
<li><strong>提交人：</strong>{{.SubmitterName}}</li>
<li><strong>提交时间：</strong>{{.SubmittedAt}}</li>
</ul>
<p>请及时登录系统进行审核。</p>
<p>系统地址：<a href="{{.SystemURL}}">{{.SystemURL}}</a></p>
<br>
<p>此邮件由系统自动发送，请勿回复。</p>
</body>
</html>`,
		},
		"report_reviewed": {
			Subject: "报告审核结果通知 - {{.ReportTitle}}",
			Content: `
<html>
<body>
<h2>报告审核结果通知</h2>
<p>您好 {{.SubmitterName}}，</p>
<p>您提交的报告已完成审核：</p>
<ul>
<li><strong>报告标题：</strong>{{.ReportTitle}}</li>
<li><strong>审核结果：</strong>{{.ReviewResult}}</li>
<li><strong>审核人：</strong>{{.ReviewerName}}</li>
<li><strong>审核时间：</strong>{{.ReviewedAt}}</li>
{{if .ReviewNotes}}<li><strong>审核备注：</strong>{{.ReviewNotes}}</li>{{end}}
</ul>
<p>请登录系统查看详细信息。</p>
<p>系统地址：<a href="{{.SystemURL}}">{{.SystemURL}}</a></p>
<br>
<p>此邮件由系统自动发送，请勿回复。</p>
</body>
</html>`,
		},
		"system_maintenance": {
			Subject: "系统维护通知",
			Content: `
<html>
<body>
<h2>系统维护通知</h2>
<p>您好，</p>
<p>系统将进行维护，具体信息如下：</p>
<ul>
<li><strong>维护时间：</strong>{{.MaintenanceTime}}</li>
<li><strong>预计时长：</strong>{{.Duration}}</li>
<li><strong>维护内容：</strong>{{.Description}}</li>
</ul>
<p>维护期间系统将暂停服务，请合理安排工作时间。</p>
<p>如有疑问，请联系系统管理员。</p>
<br>
<p>此邮件由系统自动发送，请勿回复。</p>
</body>
</html>`,
		},
	}
}
