-- ============================================================
-- ShardingSphere 分表建表 SQL
-- ============================================================
-- 在 smbms 数据库里执行这两条语句，创建 user_0 和 user_1 两个结构完全一样的表
-- 注意：不要创建 user 表！ShardingSphere 会自动帮你路由到 user_0/user_1
-- ============================================================

-- 切换到 smbms 库
USE smbms;

-- 创建第一个分表 user_0
CREATE TABLE IF NOT EXISTS user_0 (
  id BIGINT NOT NULL PRIMARY KEY,
  name VARCHAR(50),
  age INT
);

-- 创建第二个分表 user_1
CREATE TABLE IF NOT EXISTS user_1 (
  id BIGINT NOT NULL PRIMARY KEY,
  name VARCHAR(50),
  age INT
);

-- 验证一下表是否创建成功
SHOW TABLES LIKE 'user_%';
DESC user_0;
DESC user_1;
