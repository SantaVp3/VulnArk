package repository

import (
	"testing"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
	"vulnark/internal/model"
	"vulnark/pkg/testutil"
)

func TestUserRepository_Create(t *testing.T) {
	// 设置测试数据库
	db := testutil.NewTestDB()
	testutil.SeedTestData(db)
	defer testutil.CleanupTestData(db)

	// 创建Repository实例
	repo := NewUserRepository(db)

	// 测试用例
	t.Run("创建用户成功", func(t *testing.T) {
		user := &model.User{
			Username:     "newuser",
			Email:        "newuser@example.com",
			PasswordHash: "$2a$10$example_hash",
			RealName:     "新用户",
			RoleID:       1,
			Status:       1,
		}

		err := repo.Create(user)
		require.NoError(t, err)
		assert.NotZero(t, user.ID)
	})

	t.Run("创建重复用户名失败", func(t *testing.T) {
		user := &model.User{
			Username:     "testuser", // 已存在的用户名
			Email:        "another@example.com",
			PasswordHash: "$2a$10$example_hash",
			RealName:     "重复用户",
			RoleID:       1,
			Status:       1,
		}

		err := repo.Create(user)
		assert.Error(t, err)
	})
}

func TestUserRepository_GetByUsername(t *testing.T) {
	// 设置测试数据库
	db := testutil.NewTestDB()
	testutil.SeedTestData(db)
	defer testutil.CleanupTestData(db)

	// 创建Repository实例
	repo := NewUserRepository(db)

	t.Run("根据用户名获取用户成功", func(t *testing.T) {
		user, err := repo.GetByUsername("testuser")
		require.NoError(t, err)
		assert.NotNil(t, user)
		assert.Equal(t, "testuser", user.Username)
		assert.Equal(t, "test@example.com", user.Email)
	})

	t.Run("用户名不存在", func(t *testing.T) {
		user, err := repo.GetByUsername("nonexistent")
		assert.Error(t, err)
		assert.Nil(t, user)
	})
}

func TestUserRepository_GetByID(t *testing.T) {
	// 设置测试数据库
	db := testutil.NewTestDB()
	testutil.SeedTestData(db)
	defer testutil.CleanupTestData(db)

	// 创建Repository实例
	repo := NewUserRepository(db)

	t.Run("根据ID获取用户成功", func(t *testing.T) {
		user, err := repo.GetByID(1)
		require.NoError(t, err)
		assert.NotNil(t, user)
		assert.Equal(t, uint(1), user.ID)
		assert.Equal(t, "testuser", user.Username)
	})

	t.Run("ID不存在", func(t *testing.T) {
		user, err := repo.GetByID(999)
		assert.Error(t, err)
		assert.Nil(t, user)
	})
}