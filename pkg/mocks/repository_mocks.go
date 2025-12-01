package mocks

import (
	"github.com/stretchr/testify/mock"
	"vulnark/internal/model"
)

// MockUserRepository Mock用户仓储
type MockUserRepository struct {
	mock.Mock
}

func (m *MockUserRepository) Create(user *model.User) error {
	args := m.Called(user)
	return args.Error(0)
}

func (m *MockUserRepository) GetByID(id uint) (*model.User, error) {
	args := m.Called(id)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).(*model.User), args.Error(1)
}

func (m *MockUserRepository) GetByUsername(username string) (*model.User, error) {
	args := m.Called(username)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).(*model.User), args.Error(1)
}

func (m *MockUserRepository) GetByEmail(email string) (*model.User, error) {
	args := m.Called(email)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).(*model.User), args.Error(1)
}

func (m *MockUserRepository) Update(id uint, req *model.UserUpdateRequest) error {
	args := m.Called(id, req)
	return args.Error(0)
}

func (m *MockUserRepository) UpdatePassword(id uint, passwordHash string) error {
	args := m.Called(id, passwordHash)
	return args.Error(0)
}

func (m *MockUserRepository) Update2FA(id uint, enabled bool, secret string) error {
	args := m.Called(id, enabled, secret)
	return args.Error(0)
}

func (m *MockUserRepository) Delete(id uint) error {
	args := m.Called(id)
	return args.Error(0)
}

func (m *MockUserRepository) List(req *model.SearchRequest) ([]*model.User, int64, error) {
	args := m.Called(req)
	if args.Get(0) == nil {
		return nil, args.Get(1).(int64), args.Error(2)
	}
	return args.Get(0).([]*model.User), args.Get(1).(int64), args.Error(2)
}

func (m *MockUserRepository) UpdateLastLogin(id uint) error {
	args := m.Called(id)
	return args.Error(0)
}

// MockRoleRepository Mock角色仓储
type MockRoleRepository struct {
	mock.Mock
}

func (m *MockRoleRepository) Create(role *model.Role) error {
	args := m.Called(role)
	return args.Error(0)
}

func (m *MockRoleRepository) GetByID(id uint) (*model.Role, error) {
	args := m.Called(id)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).(*model.Role), args.Error(1)
}

func (m *MockRoleRepository) GetByName(name string) (*model.Role, error) {
	args := m.Called(name)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).(*model.Role), args.Error(1)
}

func (m *MockRoleRepository) Update(id uint, req *model.RoleUpdateRequest) error {
	args := m.Called(id, req)
	return args.Error(0)
}

func (m *MockRoleRepository) Delete(id uint) error {
	args := m.Called(id)
	return args.Error(0)
}

func (m *MockRoleRepository) List(req *model.SearchRequest) ([]*model.Role, int64, error) {
	args := m.Called(req)
	if args.Get(0) == nil {
		return nil, args.Get(1).(int64), args.Error(2)
	}
	return args.Get(0).([]*model.Role), args.Get(1).(int64), args.Error(2)
}