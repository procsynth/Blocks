import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import processing.core.*;

public class Grid{

	PApplet p;

	private List<Block> blocks;
	// private List<Path> paths;
	private List<List<PVector>> paths;

	private int gridSize = 50;
	private PVector pxOffset = new PVector(0,0);

	private PVector start = null;
	private PVector end = null;

	private boolean click = false;

	public Grid(PApplet p){
		this.p = p;
		blocks = new ArrayList<Block>();
		paths = new ArrayList<List<PVector>>();
	}

	public void addBlock(Block b){
		blocks.add(b);
	}

	public void draw(){

		p.translate(pxOffset.x, pxOffset.y);

		int gmousex = (int) Math.round((p.mouseX-pxOffset.x)/gridSize);
		int gmousey = (int) Math.round((p.mouseY-pxOffset.y)/gridSize);

		for(List<PVector> path : paths){
			drawPath(path, 105,240,174, 50);
		}

		if(start != null){
			drawPath(AStar((int)Math.floor(start.x), (int)Math.floor(start.y), gmousex, gmousey), 0,230,118, 255); 
		}else if(end != null){
			drawPath(AStar(gmousex, gmousey, (int)Math.floor(end.x), (int)Math.floor(end.y)), 0,230,118, 255); 
		}

		for(Block b : blocks){
			p.fill(55,71,79); // #37474F
			p.noStroke();
			p.rect(b.x()*gridSize, b.y()*gridSize, b.w()*gridSize+1, b.h()*gridSize+1);

			p.stroke(38,50,56); // #263238
			p.strokeWeight(5);

			p.fill(100,255,218); // #64FFDA // 00BFA5
			int i = 1;
			for(InNode a : b.getContentInNodes()){

				if(gmousex == b.x() && gmousey == i+b.y()){p.fill(0,191,165);
				}else if(start != null && (start.x != b.x() || start.y != i+b.y())){p.fill(84,110,122);
				}else{p.fill(100,255,218);}	 

				p.ellipse(b.x()*gridSize, (i+b.y())*gridSize, gridSize/2, gridSize/2);
				checkClickedNode(b.x(), i+b.y(), gmousex, gmousey, 1);
				i++;
			}
			i = 1;
			for(InNode a : b.getControlInNodes()){	

				if(gmousex == i+b.x() && gmousey == b.y()){p.fill(0,191,165);
				}else if(start != null && (start.x != i+b.x() || start.y != b.y())){p.fill(84,110,122);
				}else{p.fill(100,255,218);}	  

				p.ellipse((i+b.x())*gridSize, b.y()*gridSize, gridSize/2, gridSize/2);
				checkClickedNode(i+b.x(), b.y(), gmousex, gmousey, 1);
				i++;
			}
			
			p.fill(255,255,0); // #FFFF00 // FFD600 255,214,0
			i = 1;
			for(OutNode a : b.getContentOutNodes()){

				if(gmousex == b.x()+b.w() && gmousey == i+b.y()){p.fill(255,214,0);
				}else if(end != null && (end.x != b.x()+b.w() || end.y != i+b.y())){p.fill(84,110,122);
				}else{p.fill(255,255,0);}	

				p.ellipse((b.x()+b.w())*gridSize, (i+b.y())*gridSize, gridSize/2, gridSize/2);
				checkClickedNode(b.x()+b.w(), i+b.y(), gmousex, gmousey, 2);
				i++;
			}
			i = 1;
			for(OutNode a : b.getControlOutNodes()){

				if(gmousex == i+b.x() && gmousey == b.h()+b.y()){p.fill(255,214,0);
				}else if(end != null && (end.x != i+b.x() || end.y != b.h()+b.y())){p.fill(84,110,122);
				}else{p.fill(255,255,0);}	  

				p.ellipse((i+b.x())*gridSize, (b.h()+b.y())*gridSize, gridSize/2, gridSize/2);
				checkClickedNode(i+b.x(), b.h()+b.y(), gmousex, gmousey, 2);
				i++;
			}
		}


		click = false;
	}

	private void checkClickedNode(int x, int y, int gx, int gy, int t){
		if(x == gx && y == gy && click){
			if(t == 1){
				start = new PVector(x,y);
			}else{
				end = new PVector(x,y);
			}
			if(start != null && end != null){
				paths.add(AStar((int)Math.floor(start.x), (int)Math.floor(start.y), (int)Math.floor(end.x), (int)Math.floor(end.y)));
				start = end = null;
			}
		}
	}

	private void drawPath(List<PVector> path, int r, int g, int b, int a){
		if(path == null) return;
		p.strokeWeight(4);

		PVector prev = null;
		for(PVector current : path){
			if(prev != null){
				p.stroke(r,g,b, a);
				p.line(prev.x*gridSize,prev.y*gridSize,current.x*gridSize,current.y*gridSize);
			}
			prev = current;
		}
	}

	public void zoom(float amount){
		gridSize += amount;
		gridSize = Math.min(200, Math.max(20, gridSize));
	}
	public void drag(PVector amount){
		pxOffset = PVector.add(pxOffset, amount);
	}

	public void click(){
		click = true;
	}

	// private Path getPath(InNode in, OutNode out){}
	// private Path getPath(int x, int y, OutNode out){}
	// private Path getPath(InNode in, int x, int y){}

	private List<PVector> AStar(int x1, int y1, int x2, int y2){

		// Helper class remembering scores, parents, coords.
		class Square{
			public int x, y, px, py, f, g, h;

			Square(int x_, int y_, int px_, int py_, int f_, int g_, int h_){
				x = x_; y = y_; px = px_; py = py_; f = f_; g = g_; h = h_;
			} 
		}

		// A* constants (tweaked to avoid zigzaging) (also, no diagonal paths)
		int G = 10;
		int Hxf = 10;
		int Hyf = 7;

		// open/closed lists
		Map<String,Square> open = new HashMap<String,Square>();
		Map<String,Square> closed = new HashMap<String,Square>();

		// generate map
		PVector[] bounds = getBounds(x1, y1, x2, y2);
		int [][] map = getMap(bounds[0], bounds[1]);

		x1 -= Math.floor(bounds[0].x);
		x2 -= Math.floor(bounds[0].x);
		y1 -= Math.floor(bounds[0].y);
		y2 -= Math.floor(bounds[0].y);

		// put the stating point
		open.put(x1+":"+y1, new Square(x1, y1, x1, y1, 0, 0, 0));
		Square current = null;

		// check start/end validity, exit with null
		if(map[x1][y1] == 1 || map[x2][y2] == 1) return null;

		while(true){

			// select minimum score in open squares
			Integer min = null;
			current = null;

			for(Square s : open.values()){if(current == null || s.f < min){current = s; min = s.f;}}
				if(current ==  null) return null;
			
			// remove it from the open list and make it current
			open.remove(current.x+":"+current.y);
			closed.put(current.x+":"+current.y, current);

			// if this is the target square : exit loop
			if(current.x == x2 && current.y == y2){
				break; // FOUND
			}else{
			// else find adjacent squares
				for(int i = 0; i < 4; i++){
					int x = (i == 0 ? current.x - 1 : (i == 2 ? current.x + 1 : current.x));
					int y = (i == 3 ? current.y - 1 : (i == 1 ? current.y + 1 : current.y));
			// process it :
				if(
					x < 0 ||
					y < 0 ||
					x > map.length -1 ||
					y > map[0].length -1 ||
					closed.containsKey(x+":"+y)
				){  // unreachable/treated block, end laps
					// invalid (1:blocked,      middle segment a not on free space         middle segment on a starting point   )
					}else if(map[x][y] == 1 || (x != x2 || y!= y2) && map[x][y] != 0 ||  (x != x1 || y!= y1) && map[x][y] == 2){
					}else{
						// heuristic score
						int h = Math.abs(x2-x)*Hxf+Math.abs(y2-y)*Hyf;
						// record processed sqaure and it's parent
						open.put(x+":"+y, new Square(x, y, current.x, current.y, G+h, G, h));
					}

				}
			}
		}

		List<PVector> path = new ArrayList<PVector>();

		// follow the path to starting square, records path without offsets :
		while(true){
			path.add(new PVector(bounds[0].x + current.x, bounds[0].y + current.y));
			current = closed.get(current.px+":"+current.py);
			if(current.x == x1 && current.y == y1){
				path.add(new PVector(bounds[0].x + current.px, bounds[0].y + current.py));
				break;
			}
		}

		return path;
	}

	private PVector[] getBounds(int x1, int y1, int x2, int y2){
		PVector min = new PVector(Math.min(x1, x2), Math.min(y1, y2));
		PVector max = new PVector(Math.max(x1, x2), Math.max(y1, y2));

		for (Block b : blocks) {
			if(b.x() < min.x) min.x = b.x();
			if(b.y() < min.y) min.y = b.y();
			if(b.x() + b.w() > max.x) max.x = b.x() + b.w();
			if(b.y() + b.h() > max.y) max.y = b.y() + b.h();
		}

		min.x -= 3;
		min.y -= 3;
		max.x += 3;
		max.y += 3;

		PVector[] r = {min, max};
		return r;
		
	}

	private int[][] getMap(PVector min, PVector max){
		int[][] map = new int[(int)Math.floor(max.x-min.x)][(int)Math.floor(max.y-min.y)]; // 0 FREE

		for (Block b : blocks) {
			for(int bx = b.x(); bx <= b.x() + b.w(); bx++){
				int x = bx - b.x();
				for(int by = b.y(); by <= b.y() + b.h(); by++){
					int y = by - b.y();
					map[(int)Math.floor(bx-min.x)][(int)Math.floor(by-min.y)] = 1; // 1 BLOCKED
					if(y == 0 && x != 0 && x <= b.topNodeCount()) map[(int)Math.floor(bx-min.x)][(int)Math.floor(by-min.y)] = 2; // 2 IN
					if(x == 0 && y != 0 && y <= b.leftNodeCount()) map[(int)Math.floor(bx-min.x)][(int)Math.floor(by-min.y)] = 2; // 2 IN
					if(x == b.w() && y != 0 && y <= b.rightNodeCount()) map[(int)Math.floor(bx-min.x)][(int)Math.floor(by-min.y)] = 3; // 3 OUT
					if(y == b.h() && x != 0 && x <= b.bottomNodeCount()) map[(int)Math.floor(bx-min.x)][(int)Math.floor(by-min.y)] = 3; // 3 OUT
				}
			}
		}

		return map;
	}


}