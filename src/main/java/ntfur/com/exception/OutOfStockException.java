package ntfur.com.exception;

public class OutOfStockException extends RuntimeException {
    private static final long serialVersionUID = 1L;
	private final String productName;
    
    public OutOfStockException(String productName) {
        super("Rất tiếc, sản phẩm " + productName + " vừa hết hàng");
        this.productName = productName;
    }
    
    public String getProductName() {
        return productName;
    }
}
