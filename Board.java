// Board.java

/**
 CS108 Tetris Board.
 Represents a Tetris board -- essentially a 2-d grid
 of booleans. Supports tetris pieces and row clearing.
 Has an "undo" feature that allows clients to add and remove pieces efficiently.
 Does not do any drawing or have any idea of pixels. Instead,
 just represents the abstract 2-d board.
*/
public class Board	{
	// Some ivars are stubbed out for you:
	private int width;
	private int height;
	private int widths[];
	private int BKwidths[];
	private int heights[];
	private int BKheights[];
	private boolean[][] grid;
	private boolean[][] backup;
	private boolean DEBUG = true;
	boolean committed;
	private int maxHeight;
	private int BKmaxHeight;
	
	// Here a few trivial methods are provided:
	
	/**
	 Creates an empty board of the given width and height
	 measured in blocks.
	*/
	public Board(int width, int height) {
		this.width = width;
		this.height = height;
		this.maxHeight =0;
		BKmaxHeight = 0;
		heights = new int[width];	//height of each col, so array size = #cols
		BKheights= new int[width];
		widths = new int[height];   //width of each row,  so array size = #rows
		BKwidths = new int[height];
		grid = new boolean[width][height];
		backup = new boolean[width][height];
		committed = true;
		for(int x=0;x<this.width;x++){				//init grid to all false and heights/widths to all zero
			for(int y=0;y<this.height;y++){
				grid[x][y] = false;
				backup[x][y] = false;
			}
		}
		
	}
	
	
	/**
	 Returns whether the board is committed currently or not.
	*/
	
	public boolean getCommitted(){	
		return committed;
	}
	
	/**
	 Returns the width of the board in blocks.
	*/
	
	public int getWidth() {
		return width;
	}
	
	
	/**
	 Returns the height of the board in blocks.
	*/
	public int getHeight() {
		return height;
	}
	
	
	/**
	 Returns the max column height present in the board from precompute ivar.
	 For an empty board this is 0.
	*/
	public int getMaxHeight() {	 
		return maxHeight; 
	}
	

	/**
	 Calculates the max column height present in the board.
	 For an empty board this is 0.
	*/
	private int calcMaxHeight() {	 
		int max = 0;
		for(int i=0;i<heights.length;i++){
			if(heights[i]>max) max=heights[i];
		}
		return max; 
	}
	
	
	
	/**
	 Checks the board for internal consistency -- used
	 for debugging.
	*/
	public void sanityCheck() {
		if (DEBUG) {
			//compute all heights and check against the heights array
			for(int col=0;col<this.width;col++){
				if(calcColumnHeight(col)!=heights[col]){
					throw new RuntimeException("Column Height "+col+" is inconsistent with heights array. Heights array says " + heights[col] + " and calculated value is " + this.calcColumnHeight(col));
				}			
			}
			for(int row=0;row<this.height;row++){
				if(calcRowWidth(row)!=widths[row]){
					throw new RuntimeException("Row width " +row+" is inconsistent with width array. Widths array says " + widths[row] + " and calculated value is " + this.calcRowWidth(row));
				}			
			}
			if(this.calcMaxHeight() != this.getMaxHeight()){
				throw new RuntimeException("Calculated Max Height is inconsistent with max height ivar");
			}
		}
	}
	
	/**
	 Given a piece and an x, returns the y
	 value where the piece would come to rest
	 if it were dropped straight down at that x.
	 
	 <p>
	 Implementation: use the skirt and the col heights
	 to compute this fast -- O(skirt length).
	*/
	public int dropHeight(Piece piece, int x) {
		int maxLanding =0;						//keeps tabs of highest origin landing value so far
		int [] skirtArray = piece.getSkirt();
		int skirtwidth = skirtArray.length;
		for(int col =x;col<(x+skirtwidth);col++){			//parse through each column that the piece is occupying
			int localMax = getColumnHeight(col)-piece.getSkirt()[col-x];  //based on this column of the piece only, the origin of the landing point would be at index y = grid column height - skirt value    
			if(localMax > maxLanding){
				maxLanding = localMax;		//update the maxLanding variable if the landing y-index for this column was greater than the running tally thus far
			}
		}
		return maxLanding; // should return the worst case landing y-value, based on the different columns of the piece versus the existing column heights beneath them
	}
	
	
	/**
	 Returns the height of the given column from a precomputed ivar--
	 i.e. the y value of the highest block + 1.
	 The height is 0 if the column contains no blocks.
	*/
	public int getColumnHeight(int x) {
		return heights[x];
	}
	/**
	 Calculates the height of the given column 	
	*/
	private int calcColumnHeight(int x) {
		int maxYindex=-1;
		for(int yIndex=0;yIndex<this.height;yIndex++){
			if(this.grid[x][yIndex]){
				maxYindex=yIndex;
			}		
		}
		return maxYindex+1; // returns the y-index above the topmost block in this column
	}
	
	/**
	 Returns the number of filled blocks in
	 the given row from a precomputed ivar.
	*/
	public int getRowWidth(int y) {
		return widths[y];
	}
	
	/**
	 calculates the number of filled blocks in
	 the given row.
	*/
	private int calcRowWidth(int y) {
		int totalRowBlocks=0;
		for(int xIndex=0;xIndex<this.width;xIndex++){
			if(this.grid[xIndex][y]){
				totalRowBlocks++;
			}
		}
		 return totalRowBlocks; 
	}
	
	/**
	 Returns true if the given block is filled in the board.
	 Blocks outside of the valid width/height area
	 always return true.
	*/
	public boolean getGrid(int x, int y) {
		if((x<0)||(x>=this.width)||(y<0) || (y>=this.height)){			//for locations out of bounds, pretend it's a filled block to maintain generality
			return true;
		}
		return grid[x][y]; 				//otherwise, return the actual grid value
	}
	
	
	public static final int PLACE_OK = 0;
	public static final int PLACE_ROW_FILLED = 1;
	public static final int PLACE_OUT_BOUNDS = 2;
	public static final int PLACE_BAD = 3;
	
	/**
	 Attempts to add the body of a piece to the board.
	 Copies the piece blocks into the board grid.
	 Returns PLACE_OK for a regular placement, or PLACE_ROW_FILLED
	 for a regular placement that causes at least one row to be filled.
	 
	 <p>Error cases:
	 A placement may fail in two ways. First, if part of the piece may falls out
	 of bounds of the board, PLACE_OUT_BOUNDS is returned.
	 Or the placement may collide with existing blocks in the grid
	 in which case PLACE_BAD is returned.
	 In both error cases, the board may be left in an invalid
	 state. The client can use undo(), to recover the valid, pre-place state.
	*/
	public int place(Piece piece, int x, int y) {
		// flag !committed problem
		if (!committed) throw new RuntimeException("place commit problem at "+x+" , "+y);   //can only do PLACE if we are committed
		this.backup();													//going into uncommitted state; first back up the grid
		this.committed = false;					//now change the committed state because we are placing
		
		int result = PLACE_OK;
		
		if( (x<0)||(x>(this.width-piece.getWidth())) || (y<0) || (y>(this.height-piece.getHeight()))){		//check if placement of piece origin leads to any part of the piece being out of bounds     
			return PLACE_OUT_BOUNDS;
		}
		
									//remember that locations off the board are reported as filled by the getGrid method, so that will handle cases where piece goes off the grid
									//now start parsing through body of piece to see if there are collisions. 
		TPoint[] body = piece.getBody();
		for(int i=0;i<body.length;i++){			//examine each TPoint
			if(getGrid((body[i].x+x),(body[i].y+y))){			//if block already exists at one of the TPoints (shifted considering the placement of piece origin)...collision...
				return PLACE_BAD;
			}
			else{
				this.grid[body[i].x+x][body[i].y+y] = true;				//otherwise...can fill in the block
			}
		}														//at this point...found no collision for entire piece placement
		if((y+piece.getHeight()) > this.maxHeight){					//valid placement, so safe to update maxHeight if we have a new maxHeight
			this.maxHeight=(y+piece.getHeight());	
		}
		for(int i =0; i<piece.getBody().length;i++){           //update heights and widths arrays by parsing through the piece body 
			int columnNum =piece.getBody()[i].x+x;        		 //literal column and row location of the Tpoint in the body
			int rowNum = piece.getBody()[i].y+y;
			if(heights[columnNum]< rowNum+1){     		//update heights and widths arrays to account for this body point
				heights[columnNum]=rowNum+1;           //remember that the "height" is the row index ABOVE the highest occupied block
			}
			widths[rowNum] = widths[rowNum]+1;			
		}
		for(int row = y;row<(y+piece.getHeight());row++){		//check if any rows that the piece occupies has been completed
			if(this.getRowWidth(row)==this.width){
				return PLACE_ROW_FILLED;
			}				
		}			
		
		sanityCheck();	
		return result;												//if no rows completed but entire piece was placed Ok, return PLACE_OK
	}
	
	
	/**
	 Helper method that copies contents of source row to destination row
	*/
	private void rowCopy(int destRow, int sourceRow) {
		int gridWidth = this.width;
		for (int x=0;x<gridWidth;x++){
			this.grid[x][destRow]= this.grid[x][sourceRow];	
		}		
		this.widths[destRow]=this.widths[sourceRow];
	}
	
	
	/**
	 Deletes rows that are filled all the way across, moving
	 things above down. Returns the number of rows cleared.
	*/
	public int clearRows() {
		if(committed){						//check if we're coming from a committed state
			this.backup();					//if so, back up the current grid before we do anything
			this.committed=false;			//doing a clearRows makes committed = false
		}
		int rowsCleared = 0;
		for(int y=0;y<=this.maxHeight;y++){		//parse over original set of rows up to max height
			if(y!=this.height){					//check for special case - make sure the row we are checking is not out of bounds of the board (since maxheight could be one above the top of the board) 
				if(this.getRowWidth(y) == this.width){
					rowsCleared++;    //for filled rows, increment the rows cleared count, and move on till you hit an unfilled row 
				}
				else{
					rowCopy(y-rowsCleared,y);   //move unfilled rows down (via copy) by the number of filled rows encountered thus far
				}	
			}
			
		}
		int oldmaxHeight = this.maxHeight;
		this.maxHeight = this.maxHeight-rowsCleared;		//update the maxHeight to account for rows deleted
		for(int y=this.maxHeight;y<oldmaxHeight;y++){		//directly fill the rest of the grid past the new MaxHeight row up to the former maxHeight (we know there is nothing but FALSE above that) with FALSE rows
			for(int x=0;x<this.width;x++){
				grid[x][y] = false;			
			}	
			widths[y]=0;									//update the widths array for all these blank rows
		}	
		for(int i=0;i<heights.length;i++){
			heights[i]=this.calcColumnHeight(i);
		}
		sanityCheck();	
		return rowsCleared;
	}


	
	

	/**
	 Reverts the board to its state before up to one place
	 and one clearRows();
	 If the conditions for undo() are not met, such as
	 calling undo() twice in a row, then the second undo() does nothing.
	 See the overview docs.
	*/
	public void undo() {
		if(!committed){					//do a swap to restore grid to the backup.  
			boolean[][] temp = grid;
			grid=backup;
			backup=temp;		//why keep the ruined grid?  You can't have grid and backup pointing to the same thing, and you don't want to create a new backup, so just point to an existing structure for the time being.  It doesn't matter that it is containing bad data, as it will get overwritten on the next place or clearRows	
			int[] tempHeights = heights;
			int[] tempWidths = widths;
			heights= BKheights;
			BKheights = tempHeights;
			widths = BKwidths;
			BKwidths = tempWidths;
			maxHeight = BKmaxHeight;
		}
		this.committed =true;	
		sanityCheck();	
	}
	
	
	/**
	 Puts the board in the committed state.
	*/
	public void commit() {
		committed = true;
	}

	/**
	 Backups up the entire main grid to the backup grid
	*/
	private void backup() {
		for(int x=0;x<this.width;x++){
			System.arraycopy(this.grid[x], 0, this.backup[x], 0, this.grid[0].length);
		}
		BKmaxHeight = maxHeight;
		System.arraycopy(heights,0,BKheights,0,heights.length);
		System.arraycopy(widths,0,BKwidths,0,widths.length);

	}
	
	/*
	 Renders the board state as a big String, suitable for printing.
	 This is the sort of print-obj-state utility that can help see complex
	 state change over time.
	 (provided debugging utility) 
	 */
	public String toString() {
		StringBuilder buff = new StringBuilder();
		for (int y = height-1; y>=0; y--) {
			buff.append('|');
			for (int x=0; x<width; x++) {
				if (getGrid(x,y)) buff.append('+');
				else buff.append(' ');
			}
			buff.append("|\n");
		}
		for (int x=0; x<width+2; x++) buff.append('-');
		return(buff.toString());
	}
}


