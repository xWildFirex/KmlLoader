import gov.nasa.worldwindx.applications.worldwindow.features.swinglayermanager.LayerTreeGroupNode;
import javax.swing.tree.DefaultTreeModel;


public class MyTreeModel extends DefaultTreeModel  {

    public MyTreeModel() {
        super(new LayerTreeGroupNode(("Root")), true);
    }

    public LayerTreeGroupNode getRootNode(){
        return (LayerTreeGroupNode) getRoot();
    }
}
