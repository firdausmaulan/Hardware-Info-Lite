package nutech.hardware.info;

import java.io.Serializable;

public class ListContent implements Serializable {
	String Left, part, Right;

	public String getLeft() {
		return Left;
	}

	public void setLeft(String Left) {
		this.Left = Left;
	}

	public String getRight() {
		return Right;
	}

	public void setRight(String Right) {
		this.Right = Right;
	}
}
