package life.qbic.portal.portlet;

import com.vaadin.server.FileResource;
import com.vaadin.server.Page;
import com.vaadin.ui.*;
import life.qbic.voronoi.VoronoiTreemapStartup;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Widgetset;
import com.vaadin.server.VaadinRequest;

import java.io.*;


/**
 * Entry point for portlet voronoi-treemap-portlet. This class derives from {@link QBiCPortletUI}, which is found in the {@code portal-utils-lib} library.
 */
@Theme("mytheme")
@SuppressWarnings("serial")
@Widgetset("life.qbic.portlet.AppWidgetSet")
public class VoronoiTreemapUI extends QBiCPortletUI {

    private static final Logger LOG = LogManager.getLogger(VoronoiTreemapUI.class);

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
    protected Layout getPortletContent(final VaadinRequest request) {
        LOG.info("Initializing layout");
        HorizontalLayout mainHorizontalLayout = new HorizontalLayout();
        VerticalLayout left = new VerticalLayout();
        configureComponents(left, mainHorizontalLayout);

        left.addComponent(uploadFile);
        select.setWidth("450px");
        left.addComponent(select);
        left.addComponent(new HorizontalLayout(button, load));
        label_selection.setWidth("300px");
        left.addComponent(label_selection);

        mainHorizontalLayout.addComponent(left);
        setContent(mainHorizontalLayout);
        LOG.info("Finished layouting");

        return mainHorizontalLayout;
    }

    private void configureComponents(VerticalLayout l, HorizontalLayout horizontalLayout) {
        LOG.info("Configuring components");
        uploadFile.addSucceededListener(receiver);

        select.addValueChangeListener(event -> label_selection.setValue("Selected: " + event.getProperty().getValue()));
        select.setRows(10);

        load.setVisible(false);
        load.setIndeterminate(true);

        button.addClickListener((Button.ClickListener) event -> {
            load.setVisible(true);
            Notification notification = new Notification("Starting algorithm",
                    "Starting the Voronoi Treemap Algorithm, this may take some time!",
                    Notification.Type.WARNING_MESSAGE, true);
            notification.show(Page.getCurrent());

            createTreemap(() -> {
                horizontalLayout.addComponent(createTreemapFrame());
                load.setVisible(false);
                tempFile.delete();
            });

        });
        LOG.info("Finished configuring components");
    }

    class FileReceiver implements Upload.Receiver, Upload.SucceededListener {
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

        public void uploadSucceeded(Upload.SucceededEvent event) {
            addColSelectItems(tempFile);
        }
    }

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
            LOG.error("Parsing the columns of the uploaded file has failed! " + e.getMessage());
        }
    }

    private VerticalLayout createTreemapFrame() {
        LOG.info("Displaying treemap from: " + VoronoiTreemapStartup.getOutputFilePath());
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
                LOG.error("Error while writing HTML file: " + e.getMessage());
                Notification notification = new Notification("HTML Writer error",
                        "Error while writing HTML file, is /tmp/ accessible? Your uploaded file may have gotten deleted already! Please upload it again \n"
                                + "Error: " + e.getMessage(),
                        Notification.Type.ERROR_MESSAGE, true);
                notification.show(Page.getCurrent());
            }

            catch (NullPointerException e) {
                LOG.error("Error while writing data - unsupported parameter passed!");
                Notification notification = new Notification("Unsupported parameter",
                        "Algorithm was passed an unsupported parameter, please examine your parameters! " + "Error: " + e.getMessage(),
                        Notification.Type.ERROR_MESSAGE, true);
                notification.show(Page.getCurrent());
            }

            UI.getCurrent().access(ready);
            UI.getCurrent().setPollInterval(-1);
        });
        t.start();
        UI.getCurrent().setPollInterval(200);
    }

}