-- Xóa constraint sai trên bảng orders (nếu tồn tại)
-- Constraint này yêu cầu customer_id là duy nhất, nhưng mỗi khách có thể đặt nhiều đơn
DROP INDEX IF EXISTS CONSTRAINT_INDEX_8;
