package solver;

import graphical.Graph;
import graphical.Vertex;

public class CfIterator extends SemanticIterator {
    public CfIterator(Graph g) {
        super(g);
    }

    @Override
    protected boolean additionalRestriction(Vertex vertex) {
        return true;
    }
}
