import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.stream.XMLStreamException;

import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.ogc.kml.KMLAbstractFeature;
import gov.nasa.worldwind.ogc.kml.KMLRoot;
import gov.nasa.worldwind.ogc.kml.impl.KMLController;
import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwindx.applications.worldwindow.features.swinglayermanager.LayerTreeNode;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;


public class KmlLoader extends JFrame {
    public WorldWindowGLCanvas windowGLCanvas;
    private MyTreeModel treeModel;
    private JFileChooser fileChooser;

    public KmlLoader(){
        prepareFileChooser();
        makeMenu();
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.add(prepareKMLTree());
        splitPane.add(prepareWorldWindow());
        this.getContentPane().add(splitPane, BorderLayout.CENTER);
    }

    private Component prepareKMLTree() {
        JTree kmlTree = new JTree();
        treeModel = new MyTreeModel();
        kmlTree.setModel(treeModel);

        JScrollPane scrollPane = new JScrollPane(kmlTree);
        Dimension minimumSize = new Dimension(100, 100);
        scrollPane.setMinimumSize(minimumSize);
        scrollPane.setSize(minimumSize);

        return scrollPane;
    }

    private Component prepareWorldWindow() {
        this.windowGLCanvas = new WorldWindowGLCanvas();
        windowGLCanvas.setPreferredSize(new Dimension(900, 800));
        windowGLCanvas.setModel(new BasicModel());
        return windowGLCanvas;
    }

    private void prepareFileChooser(){
        fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("KML file", "kml"));
    }

    private void makeMenu() {
        JMenuBar menuBar = new JMenuBar();
        this.setJMenuBar(menuBar);
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);

        JMenuItem openFileMenuItem = new JMenuItem(new AbstractAction("Open File...") {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    openFile();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        fileMenu.add(openFileMenuItem);
    }

    private void openFile() {
        int status = fileChooser.showOpenDialog(this);
        if (status == JFileChooser.APPROVE_OPTION) {
            for (File file : fileChooser.getSelectedFiles()) {
                new WorkThread(file, this).start();
            }
        }
    }

    private class WorkThread extends Thread {
        protected File kmlFile;
        protected KmlLoader frame;

        public WorkThread(File kmlFile, KmlLoader frame) {
            this.kmlFile = kmlFile;
            this.frame = frame;
        }

        public void run() {
            try {
                KMLRoot kmlRoot = this.parse();
                kmlRoot.setField(AVKey.DISPLAY_NAME, forName(this.kmlFile, kmlRoot));

                final KMLRoot finalKMLRoot = kmlRoot;
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        frame.addKMLLayer(finalKMLRoot);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private KMLRoot parse() throws IOException, XMLStreamException {
            return KMLRoot.createAndParse(this.kmlFile);
        }
    }

    private void addKMLLayer(KMLRoot finalKMLRoot) {
        KMLController kmlController = new KMLController(finalKMLRoot);

        RenderableLayer layer = new RenderableLayer();
        layer.setName((String) finalKMLRoot.getField(AVKey.DISPLAY_NAME));

        layer.addRenderable(kmlController);
        this.windowGLCanvas.getModel().getLayers().add(layer);
        treeModel.getRootNode().add(new LayerTreeNode(layer));
        treeModel.reload();
    }

    private String forName(File kmlFile, KMLRoot kmlRoot) {

        KMLAbstractFeature rootFeature = kmlRoot.getFeature();
        if(rootFeature != null && !WWUtil.isEmpty(rootFeature.getName()))
            return rootFeature.getName();

        return kmlFile.getName();

    }



    public static void main(String[] args) {
        Configuration.setValue(AVKey.INITIAL_LATITUDE, 40);
        Configuration.setValue(AVKey.INITIAL_LONGITUDE, -101);
        Configuration.setValue(AVKey.INITIAL_ALTITUDE, 720e4);
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame frame = new KmlLoader();
                frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                frame.pack();
                frame.setVisible(true);
            }
        });
    }
}
