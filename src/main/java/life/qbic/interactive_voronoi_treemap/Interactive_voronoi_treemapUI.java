package life.qbic.interactive_voronoi_treemap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import com.vaadin.server.FileResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.TwinColSelect;
import com.vaadin.ui.UI;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;
import com.vaadin.ui.VerticalLayout;
import life.qbic.voronoi.VoronoiTreemapStartup;

@SuppressWarnings("serial")
public class Interactive_voronoi_treemapUI extends UI {
    private static final Logger LOG = LogManager.getLogger(Interactive_voronoi_treemapUI.class);

    TwinColSelect select = new TwinColSelect("Select column names in hierarchical order");
    Button button = new Button("Create Treemap");

    ProgressBar load = new ProgressBar();

    Label label_selection = new Label();
    Label label_in = new Label();
    Label label_err = new Label();

    File tempFile;
    FileReceiver receiver = new FileReceiver();
    Upload uploadFile = new Upload("Upload file to be mapped", receiver);


    @Override
    protected void init(VaadinRequest request) {
        LOG.info("Initializing layout");
        HorizontalLayout main = new HorizontalLayout();
        VerticalLayout left = new VerticalLayout();
        configureComponents(left, main);

        left.addComponent(uploadFile);
        left.addComponent(select);
        left.addComponent(new HorizontalLayout(button, load));
        left.addComponent(label_selection);
        left.addComponent(new Label("1.1.4-SNAPSHOT"));

        main.addComponent(left);
        setContent(main);
        LOG.info("Finished layouting");
    }

    private void configureComponents(VerticalLayout l, HorizontalLayout horizontalLayout) {
        LOG.info("Configuring components");
        uploadFile.addSucceededListener(receiver);

        select.addValueChangeListener(event -> label_selection.setCaption("Selected: " + event.getProperty().getValue()));
        select.setRows(10);

        load.setVisible(false);
        load.setIndeterminate(true);

        button.addClickListener((Button.ClickListener) event -> {
            load.setVisible(true);

            createTreemap(() -> {
                horizontalLayout.addComponent(createTreemapFrame());
                load.setVisible(false);
                tempFile.delete();
            });

        });
        LOG.info("Finished configuring components");
    }

    class FileReceiver implements Receiver, SucceededListener {
        @Override
        public OutputStream receiveUpload(String filename, String mimeType) {
            try {
                LOG.info("Creating temporary file for the uploaded file");
                tempFile = File.createTempFile("temp", ".csv");
                return new FileOutputStream(tempFile);
            } catch (IOException e) {
                LOG.error("Unable to receive uploaded file!");
                return null;
            }
        }

        public void uploadSucceeded(SucceededEvent event) {
            addColSelectItems(tempFile);
        }
    };

    private void addColSelectItems(File file) {
        select.removeAllItems();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String[] colNames = null;
            String[] nextLine;
            if ((nextLine = reader.readLine().split("\t")) != null)
                colNames = nextLine;
            reader.close();

            for (String c : colNames) {
                select.addItem(c);
            }
        } catch (IOException e) {
            LOG.info("Parsing the columns of the uploaded file has failed! " + e.getMessage());
        }
    }

    private VerticalLayout createTreemapFrame() {
        LOG.info("Displaying treemap from: " + VoronoiTreemapStartup.getOutputFilePath());
        //File testFile = new File(Interactive_voronoi_treemapUI.class.getClassLoader().getResource("voroTreemap123071972158065013.html").getFile());
        //BrowserFrame browserStuff = new BrowserFrame("blabla", new FileResource(testFile));
        BrowserFrame browser = new BrowserFrame("Voronoi Treemap", new FileResource(new File(VoronoiTreemapStartup.getOutputFilePath())));

        browser.setWidth("1500px");
        browser.setHeight("1000px");

        return new VerticalLayout(browser);
    }

    /**
     * creates the Treemap
     * saves it automatically in /tmp
     * If you do NOT want it to be saved and deleted in /tmp
     * use the -o option and change the VoroTreemapOutputFilePath and remove the -t option
     * resize the array accordingly
     *
     * @param ready
     */
    public void createTreemap(final Runnable ready) {
        LOG.info("Starting algorithm thread");
        Thread t = new Thread(() -> {
            LOG.info("Creating treemap");
            LOG.info("Setting up treemap algorithm options");
            String columns = "";

            if (!select.isEmpty())
                columns = select.getValue().toString();

            columns = columns.substring(1, columns.length() - 1); //remove brackets

            String[] allColumns = columns.split(",");
            for (int i = 0; i < allColumns.length; i++) {
                allColumns[i] = allColumns[i].trim();
            }

            // all parameter identifiers + filePaths + temporary = 6
            // all parameter identifiers + temporary = 4
            int inputSize = 4 + allColumns.length;
            String[] input = new String[inputSize];
            input[0] = "-f";
            input[1] = tempFile.getAbsolutePath();
            input[2] = "-c";
            //input[input.length - 3] = "-o";
            //input[input.length - 2] = voroTreemapOutputFilePath;
            input[input.length - 1] = "-t";

            //insert all columns at their respective index
            for (int i = 0; i < allColumns.length; i++) {
                input[i + 3] = allColumns[i];
            }

            //create the treemap
            try {
                LOG.info("Starting Treemap generation algorithm in the portlet");
                VoronoiTreemapStartup.createTreemap(input);
            } catch (IOException e) {
                LOG.error("Creation of Treemap has failed! " + e.getMessage());
            }

            UI.getCurrent().access(ready);
            UI.getCurrent().setPollInterval(-1);
        });
        t.start();
        UI.getCurrent().setPollInterval(200);

        try {
            t.join();
        } catch (InterruptedException e) {
            LOG.info("Unable to end algorithm computation thread: " + e.getMessage());
        }

        LOG.info("Algorithm thread is alive: " + t.isAlive());
    }

}
