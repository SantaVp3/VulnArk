package service

import (
	"errors"
	"testing"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/mock"
	"github.com/stretchr/testify/require"
	"vulnark/internal/model"
	"vulnark/pkg/mocks"
)

func TestUserService_GetUserByID(t *testing.T) {
	// 创建Mock Repository
	mockUserRepo := new(mocks.MockUserRepository)
	mockRoleRepo := new(mocks.MockRoleRepository)

	// 创建Service实例
	service := NewUserService(mockUserRepo, mockRoleRepo)

	t.Run("获取用户成功", func(t *testing.T) {
		// 设置Mock期望
		expectedUser := &model.User{
			ID:       1,
			Username: "testuser",
			Email:    "test@example.com",
			RealName: "测试用户",
			RoleID:   1,
		}
		mockUserRepo.On("GetByID", uint(1)).Return(expectedUser, nil)

		// 执行测试
		user, err := service.GetUserByID(1)

		// 验证结果
		require.NoError(t, err)
		assert.Equal(t, expectedUser, user)
		mockUserRepo.AssertExpectations(t)
	})

	t.Run("用户不存在", func(t *testing.T) {
		// 设置Mock期望
		mockUserRepo.On("GetByID", uint(999)).Return(nil, errors.New("record not found"))

		// 执行测试
		user, err := service.GetUserByID(999)

		// 验证结果
		assert.Error(t, err)
		assert.Nil(t, user)
		mockUserRepo.AssertExpectations(t)
	})
}

func TestUserService_CreateUser(t *testing.T) {
	// 创建Mock Repository
	mockUserRepo := new(mocks.MockUserRepository)
	mockRoleRepo := new(mocks.MockRoleRepository)

	// 创建Service实例
	service := NewUserService(mockUserRepo, mockRoleRepo)

	t.Run("创建用户成功", func(t *testing.T) {
		// 准备测试数据
		req := &model.UserCreateRequest{
			Username: "newuser",
			Email:    "newuser@example.com",
			Password: "password123",
			RealName: "新用户",
			RoleID:   1,
		}

		// 设置Mock期望
		mockUserRepo.On("GetByUsername", "newuser").Return(nil, errors.New("record not found"))
		mockUserRepo.On("GetByEmail", "newuser@example.com").Return(nil, errors.New("record not found"))
		
		// 检查角色是否存在
		expectedRole := &model.Role{ID: 1, Name: "测试角色"}
		mockRoleRepo.On("GetByID", uint(1)).Return(expectedRole, nil)
		
		// 创建用户
		mockUserRepo.On("Create", mock.AnythingOfType("*model.User")).Return(nil)

		// 执行测试
		user, err := service.CreateUser(req)

		// 验证结果
		require.NoError(t, err)
		assert.NotNil(t, user)
		assert.Equal(t, "newuser", user.Username)
		assert.Equal(t, "newuser@example.com", user.Email)
		assert.Equal(t, uint(1), user.RoleID)
		mockUserRepo.AssertExpectations(t)
		mockRoleRepo.AssertExpectations(t)
	})

	t.Run("用户名已存在", func(t *testing.T) {
		// 准备测试数据
		req := &model.UserCreateRequest{
			Username: "existinguser",
			Email:    "new@example.com",
			Password: "password123",
			RealName: "用户",
			RoleID:   1,
		}

		// 设置Mock期望 - 用户名已存在
		existingUser := &model.User{ID: 1, Username: "existinguser"}
		mockUserRepo.On("GetByUsername", "existinguser").Return(existingUser, nil)

		// 执行测试
		user, err := service.CreateUser(req)

		// 验证结果
		assert.Error(t, err)
		assert.Nil(t, user)
		assert.Contains(t, err.Error(), "用户名已存在")
		mockUserRepo.AssertExpectations(t)
	})
}