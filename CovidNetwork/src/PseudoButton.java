
public class PseudoButton {

	//stores x/y coordinates and tells if you clicked inside
	
	
	
	public int left;
	public int right;
	public int top;
	public int bottom;
	
	
	public PseudoButton(int l, int t, int r, int b) {
		left = l;
		top = t;
		right = r;
		bottom = b;
	}
	
	
	public boolean inside(int x, int y) {
		if(left <= x && x < right && top <= y && y < bottom) return true;
		else return false;
	}
	
	
	
	
}
