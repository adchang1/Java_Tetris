// JTetris.java
/**
 CS108 Tetris Game with auto-play Brain.
 This is an implementation of JTetris that adds the option of enabling an AI
 "Brain" that will play the game by itself.

*/
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class JBrainTetris extends JTetris{
	protected JCheckBox brainmode;  
	protected JPanel little;
	protected JSlider adversary;
	protected JLabel OKLabel;
	protected DefaultBrain dBrain = new DefaultBrain();				//uses the provided default Brain as the AI player
	protected Brain.Move goodMove = new Brain.Move();
	
	public JBrainTetris(int pixels){
		super(pixels);    //uses the superclass's constructor
		
	}
	
	/**
	 Sets the enabling of the start/stop buttons
	 based on the gameOn state. Copied from superclass because it was private there
	 and thus we couldn't use it.  
	*/
	
	private void enableButtons() {
		startButton.setEnabled(!gameOn);
		stopButton.setEnabled(gameOn);
	}
	
	
	@Override()
	public JComponent createControlPanel(){
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		// COUNT
		countLabel = new JLabel("0");
		panel.add(countLabel);
		
		// SCORE
		scoreLabel = new JLabel("0");
		panel.add(scoreLabel);
		
		// TIME 
		timeLabel = new JLabel(" ");
		panel.add(timeLabel);

		panel.add(Box.createVerticalStrut(12));
		
		// START button
		startButton = new JButton("Start");
		panel.add(startButton);
		startButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				startGame();
			}
		});
		
		// STOP button
		stopButton = new JButton("Stop");
		panel.add(stopButton);
		stopButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stopGame();
			}
		});
		
		enableButtons();
		
		JPanel row = new JPanel();
		
		// SPEED slider
		panel.add(Box.createVerticalStrut(12));
		row.add(new JLabel("Speed:"));
		speed = new JSlider(0, 200, 75);	// min, max, current
		speed.setPreferredSize(new Dimension(100, 15));
		
		updateTimer();
		row.add(speed);
		
		panel.add(row);
		speed.addChangeListener( new ChangeListener() {
			// when the slider changes, sync the timer to its value
			public void stateChanged(ChangeEvent e) {
				updateTimer();
			}
		});
		
		
		testButton = new JCheckBox("Test sequence");
		panel.add(testButton);
		
		////ADDED brain options
		panel.add(new JLabel("Brain:"));
		brainmode = new JCheckBox("Brain active"); 
		panel.add(brainmode);
		
		// make a little panel, put a JSlider in it. JSlider responds to getValue() 
		little = new JPanel();
		little.add(new JLabel("Adversary:"));
		OKLabel = new JLabel("ok");
		little.add(OKLabel);
		adversary = new JSlider(0, 100, 0); // min, max, current 
		adversary.setPreferredSize(new Dimension(100,15)); 
		little.add(adversary);
		// now add little to panel of controls
		panel.add(little);
		return panel;
		
	}
	
	/**
	 Selects the next piece to use using the random generator
	 set in startGame(). Overriden version allows Adversary to pick nonoptimal piece.
	*/
	@Override
	public Piece pickNextPiece() {	
		int rando = (int)(100*random.nextDouble());
		Piece piece;
		if((adversary.getValue()==100) || (adversary.getValue() > rando)){  //if slider is high enough, pick a tough piece
			OKLabel.setText("*ok*");			
			Brain.Move testMove;
			int worstIndex=0;
			double worstScore = 0;         //set to an unreachable score initially
			for(int i=0;i<pieces.length;i++){
				board.undo();
				testMove = dBrain.bestMove(board, pieces[i], HEIGHT, null);    //parse through all pieces types
				if(testMove.score>worstScore){			//check if this piece has the worst possible "best" score
					worstIndex=i;						//if so...update the worst index and worst score
					worstScore = testMove.score;
				}
			}   //at this point, worstIndex contains the piece index that has the worst "best" score.
			piece = pieces[worstIndex];
		}
		
		else{  							//pick a normal random piece
			OKLabel.setText("ok");
			int pieceNum;	
			pieceNum = (int) (pieces.length * random.nextDouble());
			piece = pieces[pieceNum];
			
		}
		
		
		return(piece);
	}
	
	
	/**
	 Updates the count/score labels with the latest values.  Had to copy it over since it's private in JTetris.
	 */
	protected void updateCounters() {
		countLabel.setText("Pieces " + count);
		scoreLabel.setText("Score " + score);
	}
	
	
	/**
	 Called to change the position of the current piece.
	 Each key press calls this once with the verbs
	 LEFT RIGHT ROTATE DROP for the user moves,
	 and the timer calls it with the verb DOWN to move
	 the piece down one square.

	 Before this is called, the piece is at some location in the board.
	 This advances the piece to be at its next location.
	 
	 Overriden by the brain when it plays.
	*/
	@Override
	public void tick(int verb) {
		if (!gameOn) return;
		
		if (currentPiece != null) {
			board.undo();	// remove the piece from its old position
		}
		
		if(brainmode.isSelected() && verb == DOWN){		//check if brain mode is active and we were ticking DOWN...if so, then...			
				
 			//have your Brain calculate the best move and return it in a Move instance.
			goodMove = dBrain.bestMove(board, currentPiece, HEIGHT, goodMove) ;
			if(goodMove!=null){
				
		  //when the timer called for downward movement, it is the brain's chance to make 1 rotation and 1 lateral movement.
				if(goodMove.piece != currentPiece){
					currentPiece = currentPiece.fastRotation();   //use a rotation if your piece isn't the optimal rotated version
				}
				if(currentX<goodMove.x){  //if we are left of the optimal x position...
					tick(RIGHT);          //move either LEFT or RIGHT towards the optimal x-location
				}
				else if(currentX>goodMove.x){
					tick(LEFT);
				}
				else if(currentX == goodMove.x){
					//no left/right movement, stay where you are. 
				}					
			}
		}
		
		if (currentPiece != null) {
			board.undo();	// remove the piece from its old position
		}

		// Sets the newXXX ivars
		computeNewPosition(verb);
		
		// try out the new position (rolls back if it doesn't work)
		int result = setCurrent(newPiece, newX, newY);
		
		// if row clearing is going to happen, draw the
		// whole board so the green row shows up
		if (result ==  Board.PLACE_ROW_FILLED) {
			repaint();
		}
		

		boolean failed = (result >= Board.PLACE_OUT_BOUNDS);
		
		// if it didn't work, put it back the way it was
		if (failed) {
			if (currentPiece != null) board.place(currentPiece, currentX, currentY);
			repaintPiece(currentPiece, currentX, currentY);
		}
	
		/*
		 How to detect when a piece has landed:
		 if this move hits something on its DOWN verb,
		 and the previous verb was also DOWN (i.e. the player was not
		 still moving it),	then the previous position must be the correct
		 "landed" position, so we're done with the falling of this piece.
		*/
		if (failed && verb==DOWN && !moved) {	// it's landed
		
			int cleared = board.clearRows();
			if (cleared > 0) {
				// score goes up by 5, 10, 20, 40 for row clearing
				// clearing 4 gets you a beep!
				switch (cleared) {
					case 1: score += 5;	 break;
					case 2: score += 10;  break;
					case 3: score += 20;  break;
					case 4: score += 40; Toolkit.getDefaultToolkit().beep(); break;
					default: score += 50;  // could happen with non-standard pieces
				}
				updateCounters();
				repaint();	// repaint to show the result of the row clearing
			}
			
			
			// if the board is too tall, we've lost
			if (board.getMaxHeight() > board.getHeight() - TOP_SPACE) {
						
				stopGame();
			}
			// Otherwise add a new piece and keep playing
			else {
				addNewPiece();   //this only happens if verb was DOWN and hit a floor, so we can call LEFT and RIGHT without worrying about spawning a new piece...
			}
		}
		// Note if the player made a successful non-DOWN move --
		// used to detect if the piece has landed on the next tick()
		moved = (!failed && verb!=DOWN);
	}

	
	/**
	 Creates a frame with a JTetris.
	*/
	public static void main(String[] args) {
		// Set GUI Look And Feel Boilerplate.
		// Do this incantation at the start of main() to tell Swing
		// to use the GUI LookAndFeel of the native platform. It's ok
		// to ignore the exception.
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ignored) { }
		
		JBrainTetris tetris = new JBrainTetris(16);
		JFrame frame = JBrainTetris.createFrame(tetris);
		frame.setVisible(true);
	}
	
	
	
	
}