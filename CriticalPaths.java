//===========================================================================================================================
//	Program : Class to compute the per chart calculations
//===========================================================================================================================
//	@author: Karthika Karunakaran
// 	Date created: 2016/12/07
//===========================================================================================================================
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Queue;
import java.util.Stack;

public class CriticalPaths {

	private static Graph g = null;
	private static Graph newGraph = null;
	private static List<Vertex> vertices = null;
	ArrayList<Vertex> pathArrays = new ArrayList<>();
	Vertex[] arrayOfPaths = null;
	int flag = 0;
	
	CriticalPaths(Graph g) {
    	this.g = g;
    }

    void findCriticalPaths() {
	//earliset completion time calculation
    	earliestCompletionTime(g.s);
	//latest completion time calculation
    	latestCompletionTime(g.s, g.t);
	//build new graph with only critical nodes and tight edges
    	graphWithOnlyCriticalNodes();
	//DFS of the new graph to find one critical path
    	List<Vertex> resutlList = new ArrayList<>();	
    	dfs(newGraph.s, resutlList, newGraph.t);
	//Length of critical path
    	System.out.println(newGraph.t.lc);
	//print one critical path	
    	for(Vertex v : resutlList)
    		System.out.print(v.name+" ");
    	System.out.println("\n");
	//Printing the tasks and details
    	print();
	//Total no of critical nodes
    	System.out.println("\n"+(vertices.size() - 2));
	//Total no of critical paths
    	noOfCriticalPaths();
    	//findPaths(newGraph.t);
	//Enumerating all the critical paths
    	arrayOfPaths = new Vertex[newGraph.size];
    	enumeratePaths(newGraph.s, 0);
    }
    
    //Total no of critical paths
    private void noOfCriticalPaths() {
    	List<Vertex> topSortOrder = new ArrayList<>();
    	topSortOrder = topologicalSort(newGraph, newGraph.s, topSortOrder);
    	for(Vertex v : newGraph) {
    		v.n = 0;
    	}
    	newGraph.s.n = 1;
    	for(Vertex v : topSortOrder) {
    		for (Edge e : v.adj) {
    			Vertex u = e.otherEnd(v);
    			u.n += v.n;//u.n = u.n + v.n;
    		}
    	}
    	System.out.println(newGraph.t.n);
    }
    
    private ArrayList<Vertex> findPaths(Vertex u) {
    	if (u == newGraph.s) return null;
    	else {
    		for (Edge edge : u.revAdj) {
    			Vertex p = edge.otherEnd(u);
    			if (p != newGraph.s)
    				pathArrays.add(p);
    			findPaths(p);
    		}
    	}
    	int size = pathArrays.size();
    	if (size > 0) 
    		System.out.print(pathArrays.remove(size - 1) + " ");
    	
		return pathArrays;
    }
    	//build new graph with only critical nodes and tight edges
	private void graphWithOnlyCriticalNodes() {
		vertices = new ArrayList<>();
    	for (Vertex v : g) {
    		if(isCriticalNode(v)) {
				vertices.add(v);
    		}
    	}
    	newGraph = new Graph(vertices.size(), true);
    	int i = 1;
    	for(Vertex v : vertices) {
    		newGraph.v.add(i, new Vertex(v.name));
    		Vertex vert = newGraph.getVertex(i);
    		v.newIndex = i;
    		//name, d, ec, lc, slack
    		vert.name = v.name;
    		vert.d = v.d;
    		vert.ec = v.ec;
    		vert.lc = v.lc;
    		vert.slack = v.slack;

    		if(vert.name == g.s.name) {
    			newGraph.s = vert;
    		} else if (vert.name == g.t.name) {
    			newGraph.t = vert;
    		}
    		i++;
    	}
    	int fromIndex = Integer.MIN_VALUE, toIndex = Integer.MIN_VALUE;;
    	for(Vertex v : g) {
    		for(Edge e : v.adj) {
    			if(isTightEdge(e)) {
	    			fromIndex = e.from.newIndex;
	    			toIndex = e.to.newIndex;
	    			newGraph.addEdge(newGraph.getVertex(fromIndex), newGraph.getVertex(toIndex), e.weight);
    			}
    		}
    	}
    	
    	for (Vertex v : newGraph) {
    		v.inDegree = v.revAdj.size();
    	}
	}

	//Enumerating all the critical paths
	public void enumeratePaths(Vertex u, int index) {
		arrayOfPaths[index] = u;
		if (u == newGraph.t)
			visit(arrayOfPaths, index);
		else {
			for (Edge e : u.adj) {
				Vertex j = e.otherEnd(u);
				enumeratePaths(j, index + 1);
			}
		}
	}
	
	public void visit(Vertex[] arrayOfVertices, int index) {
		for (int i = 1; i < index; i++) {
			System.out.print(arrayOfVertices[i] + " ");
		}
		System.out.println();
	}
    
    //Printing the tasks and details
    private void print() {
    	System.out.println("Task\tEC\tLC\tSlack");
    	for (Vertex v : g) {
    		if(v != g.s && v != g.t)
    			System.out.println(v.name+"\t"+v.ec+"\t"+v.lc+"\t"+v.slack);
    	}
    	
	}
	//Method to check if the edge is tight edge
	public static boolean isTightEdge(Edge e) {
    	Vertex from = e.from;
    	Vertex to = e.to;
    	if(to.lc == from.lc + to.d) {
    		return isCriticalNode(to) && isCriticalNode(from);
    	} else 
    		return false;
    }
    
    //Method to check if the vertex is critical node
    public static boolean isCriticalNode(Vertex v) {
    	return (v.slack == 0);
    }

    //earliset completion time calculation
    public static void earliestCompletionTime(Vertex s) {
    	List<Vertex> topSortOrder = new LinkedList<>();
    	topologicalSort(g, s, topSortOrder);
    	for (Vertex v : g) {
    		v.ec = v.d;
    	}
    	s.ec = 0;
    	for(Vertex v : topSortOrder) {
    		for (Edge e : v.adj) {
    			Vertex u = e.otherEnd(v);
    			u.ec = Math.max(u.ec, v.ec + u.d);
    		}
    	}
    }

    //latest completion time calculation
    public static void latestCompletionTime(Vertex s, Vertex t) {
    	List<Vertex> topSortOrder = new LinkedList<>();
    	dfs(g, s, topSortOrder);
    	t.lc = t.ec;
    	for (Vertex v : g) {
    		v.lc = t.lc;
    	}
    	ListIterator<Vertex> revIt = topSortOrder.listIterator(topSortOrder.size());
    	while (revIt.hasPrevious()) {
    		Vertex v = revIt.previous();
    		for(Edge e : v.revAdj) {
    			Vertex u = e.otherEnd(v);
    			u.lc = Math.min(u.lc, v.lc - v.d);
    			u.slack = u.lc - u.ec;
    		}
    	}
    }
    
    //Topological sort with indegree
    public static List<Vertex> topologicalSort(Graph g, Vertex s, List<Vertex> topSortOrder) {
    	Queue<Vertex> queue = new LinkedList<>();
    	for (Vertex v : g) {
    		if(v.inDegree == 0)
    			queue.add(v);
    	}
    	int count = 0;
    	while (!queue.isEmpty()) {
    		Vertex v = queue.remove();
    		topSortOrder.add(v);
    		v.top = ++count;
    		for (Edge e : v.adj) {
    			Vertex vert = e.otherEnd(v);
    			vert.inDegree--;
    			if (vert.inDegree == 0) {
    				queue.add(vert);
    			}
    		}
    	}
    	if (count != g.size) {
    		return null;
    	}
		return topSortOrder;
    }
    
    //Topological sort
    public static List<Vertex> dfs(Graph g, Vertex s, List<Vertex> topSortOrder) {
		for (Vertex v : g) {
			v.seen = false;
		}
		for (Vertex v : g) {
			if (!v.seen) {
				topSortOrder = dfsVisit(v, topSortOrder);
				topSortOrder.add(0, v);
			} 
		}
		return topSortOrder;
	}

	public static List<Vertex> dfsVisit(Vertex v, List<Vertex> topSortOrder) {
		v.seen = true;
		for (Edge e : v.adj) {
			Vertex vert = e.otherEnd(v);
			if (!vert.seen) {
				topSortOrder = dfsVisit(vert, topSortOrder);
				topSortOrder.add(0, vert);
			}
		}
		return topSortOrder;
	}
	//DFS of the new graph to find one critical path
	 public void dfs(Vertex src, List<Vertex> result, Vertex t)
	    {       
	        //Avoid infinite loops
	        if(src == t) return;
	        if (src != newGraph.s)
	        	result.add(src);
	        src.seen = true;

	        //for every child
	        for(Edge e: src.adj)
	        {
	        	Vertex v = e.otherEnd(src);
	            //if childs state is not visited then recurse
	            if(!v.seen && v != t && flag == 0)
	                dfs(v, result, t);
	            else if(v == t) {
	            	flag = 1;
	            	break;
	            }
	        }
	        return;
	 }
}
