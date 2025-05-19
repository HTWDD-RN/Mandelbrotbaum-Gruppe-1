import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

class UpdateRequest implements Runnable {
    private final Model model;
    private final MandelbrotView view;

    public UpdateRequest(Model model, MandelbrotView view) {
        this.model = model;
        this.view = view;
    }

    public void run() {
        model.drawMandelbrot();
        view.repaint();
    }
}

class Ticker extends Thread {
    private final Model model;
    private final MandelbrotView view;
    private static final long UPDATE_INTERVAL = 100;
    private final UpdateRequest updateRequest;

    public Ticker(Model model, MandelbrotView view){
        this.model = model;
        this.view = view;
        updateRequest = new UpdateRequest(model, view);
    }

    public void run() {
        try{
            while(!isInterrupted()) {
                EventQueue.invokeLater(updateRequest);
                Thread.sleep(UPDATE_INTERVAL);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

class Model {

    private final BufferedImage image;
    private int height;
    private int width;

    public Model(int width, int height) {
        this.width = width;
        this.height = height;
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public synchronized BufferedImage getImage() {
        return image;
    }

    public synchronized void drawMandelbrot () {
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(Color.GRAY);
        g2d.fillRect(0,0, width, height);
        g2d.dispose();
    }
}



class MandelbrotView extends JPanel {
    
    private final Model model;
    private JFrame frame;
    private final String[] s1 = {"1024 x 768", "1920 x 1080"};

    public MandelbrotView(Model model) {
        this.model = model;
    }

    public void initView() {
        frame = new JFrame("Mandelbrot Gruppe 1");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridLayout(4, 4, 2, 2));
        panel.setBorder(BorderFactory.createEmptyBorder(10,10, 10, 10));
        
        panel.add(new JLabel("Auflösung: "));
        panel.add(new JComboBox<String>(s1));

        panel.add(new JLabel("Zoomfaktor: ")); 
        panel.add(new JSpinner(new SpinnerNumberModel(0.8,0,10,0.2)));

        panel.add(new JLabel("Stufenanzahl: "));
        panel.add(new JSpinner(new SpinnerNumberModel(100, 0, 100, 1)));

        panel.add(new JLabel("Anzahl Worker: "));
        panel.add(new JSpinner(new SpinnerNumberModel(4, 2, 16, 2)));

        panel.add(new JLabel("Iterationsanzahl: "));
        panel.add(new JSpinner(new SpinnerNumberModel(100, 1, 1000, 1)));

        panel.add(new JLabel(""));
        panel.add(new JLabel(""));

        panel.add(new JLabel("Zoompunkt: "));
        panel.add(new JTextField("x-position",16));
        panel.add(new JTextField("y-position",16));

        panel.add(new JButton("Ausführen"));

        frame.add(panel, BorderLayout.NORTH);
        frame.add(this, BorderLayout.CENTER);

        frame.setSize(model.getWidth() + 40 , model.getHeight() + 220);
        frame.setVisible(true);
    }

    @Override
    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        synchronized (model){
            g.drawImage(model.getImage(),10, 40, this);
        }
    }
}

class MandelbrotPresenter {

    private final Model model;
    private final MandelbrotView view;
    private final Ticker ticker;

    public MandelbrotPresenter(Model model, MandelbrotView view) {
        this.model = model;
        this.view = view;
        this.ticker = new Ticker(model, view);
    }

    public void start() {
        view.initView();
        ticker.start();
    }
}


public class Mandelbrotmenge {

    public static void main(String[] args) throws InterruptedException {
        Model model = new Model(1024,768);
        MandelbrotView view = new MandelbrotView(model);
        MandelbrotPresenter presenter = new MandelbrotPresenter(model, view);
        presenter.start();
    }

}