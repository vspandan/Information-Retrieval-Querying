
public class HandleUserException extends Exception{
	private static final long serialVersionUID = -666526072767696624L;
	private String msg="";
	public HandleUserException(String msg) {
		this.msg=msg;
	}
	@Override
	public String getMessage() {
		return msg;
	}
}
