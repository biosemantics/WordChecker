/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui.common;

import java.lang.reflect.*;
import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import com.lizardtech.djvu.DjVuOptions;

/**
 *
 * @author iychoi
 */
public class DjvuView extends java.awt.Frame implements AppletStub, Runnable {

    //~ Static fields/initializers ---------------------------------------------
    private static final Class classApplet;
    private static final Method isValidDjVuMethod;
    private static final String classAppletName = "com.lizardtech.djview.Applet";

    static {
        Class xclassApplet = null;
        try {
            xclassApplet = Class.forName(classAppletName);
        } catch (final ClassNotFoundException exp) {
            exp.printStackTrace(DjVuOptions.err);
            System.exit(1);
        }
        classApplet = xclassApplet;
        Method xisValidDjVuMethod = null;
        try {
            xisValidDjVuMethod = classApplet.getMethod("isValidDjVu", (Class[]) null);
        } catch (final Throwable exp) {
            exp.printStackTrace(DjVuOptions.err);
            System.exit(1);
        }
        isValidDjVuMethod = xisValidDjVuMethod;
    }
    //~ Instance fields --------------------------------------------------------
    private final AppletContext appletContext;
    /**
     * Create an applet for this document.
     */
    private final Hashtable parameters = new Hashtable();
    private final Panel panel = new Panel();
    private String currentURL = null;
    private URL documentBase = null;
    private boolean isDjVu = false;
    
    private com.lizardtech.djview.Applet djvuApplet;

    //~ Constructors -----------------------------------------------------------
    /**
     * Creates a new Frame object.
     */
    public DjvuView() {
        this(null);
    }

    /**
     * Creates a new Frame object.
     *
     * @param url The initial URL to load.
     */
    public DjvuView(String url) {
        appletContext = new AppletContext() {
            public void showStatus(final String status) {
                DjVuOptions.out.println("status: " + status);
            }

            public void showDocument(final URL url, String target) {
                final DjvuView f = new DjvuView();
                f.setURL(url);
                (new Thread(f)).start();
            }

            public void showDocument(final URL url) {
                setURL(url);
            }

            public void setStream(final String key, InputStream stream) {
            }

            public Iterator getStreamKeys() {
                return null;
            }

            public InputStream getStream(final String key) {
                return null;
            }

            public Image getImage(final URL url) {
                return null;
            }

            public AudioClip getAudioClip(final URL url) {
                return null;
            }

            public Enumeration getApplets() {
                return null;
            }

            public Applet getApplet(final String name) {
                return null;
            }
        };

        try {
            documentBase =
                    (new File(
                    System.getProperties().getProperty("user.dir", "/"),
                    "/index.djvu")).toURL();
        } catch (final Throwable ignored) {
        }

        setURL(url);
    }

    //~ Methods ----------------------------------------------------------------
    /**
     * Get the applet context, used for retrieving web contents.
     *
     * @return null
     */
    public AppletContext getAppletContext() {
        return appletContext;
    }

    /**
     * Get the codebase for the applet.
     *
     * @return null.
     */
    public URL getCodeBase() {
        return null;
    }

    /**
     * Get the document base for the applet.
     *
     * @return null.
     */
    public URL getDocumentBase() {
        return documentBase;
    }

    /**
     * Lookup a parameter.
     *
     * @param name The name of the parameter to lookup.
     *
     * @return the parameter value.
     */
    public String getParameter(String name) {
        Object retval = parameters.get(name);

        return (retval != null)
                ? retval.toString()
                : null;
    }

    /**
     * Set the URL to display in this frame.
     *
     * @param url to display.
     */
    public void setURL(final String url) {
        if ((url == null) || (url.length() == 0)) {
            setURL((URL) null);
        } else {
            try {
                setURL(new URL(
                        getDocumentBase(),
                        url));
            } catch (final Throwable ignored) {
            }
        }
    }

    private void printFile(final StringBuffer text, final File x, final String name)
            throws MalformedURLException {
        if (x.canRead()) {
            String size = "-";
            if (!x.isDirectory()) {
                double s = ((double) x.length()) / 1024D;
                if (s >= 1024D) {
                    size = (Math.ceil(s / 102.4D) / 10D) + "M";
                } else {
                    size = ((int) Math.ceil(s) + "K");
                }
            }
            text.append("<tr><td>&nbsp;</td><td><a href=\"" + (x.toURL()) + "\">" + name + "</a></td><td align=\"right\">" + (new Date(x.lastModified())) + "</td><td align=\"right\">" + size + "</td></tr>\n");
        }
    }

    /**
     * Set the URL to display in this frame.
     *
     * @param url to display.
     */
    public void setURL(final URL url) {
        String urlString = "";

        Component component = null;
        if (url != null) {
            urlString = url.toString();

            JEditorPane jeditorPane = null;
            if (url.getProtocol().equals("file")) {
                File f = new File(url.getPath());
                if (f.isDirectory()) {
                    final String path = f.getAbsolutePath();
                    final StringBuffer text = new StringBuffer("<html><head>Index of " + path + "</head><body><h1>Index of " + path + "</h1><table><tr><td>&nbsp;&nbsp;&nbsp;&nbsp;</td><td><b>Name</b></td><td><b>Last Modified</b></td><td><b>Size</b></td></tr>\n");
                    try {
                        printFile(text, f.getParentFile(), "<em>Parent Directory</em>");
                    } catch (final Throwable exp) {
                        final File[] roots = f.listRoots();
                        for (int i = 0; i < roots.length; i++) {
                            try {
                                final File x = roots[i];
                                printFile(text, x, "<em>" + x.getAbsolutePath() + "</em>");
                            } catch (final Throwable ignored) {
                            }
                        }
                    }
                    final File[] list = f.listFiles();
                    for (int i = 0; i < list.length; i++) {
                        try {
                            final File x = list[i];
                            if (x.isDirectory() && !x.isHidden()) {
                                printFile(text, x, x.getName() + File.separator);
                            }
                        } catch (final Throwable ignored) {
                        }
                    }
                    for (int i = 0; i < list.length; i++) {
                        try {
                            final File x = list[i];
                            if (!x.isDirectory() && !x.isHidden()) {
                                printFile(text, x, x.getName());
                            }
                        } catch (final Throwable ignored) {
                        }
                    }
                    text.append("</table></body></html>");
                    jeditorPane = new JEditorPane("text/html", text.toString());
                }
            }
            if (jeditorPane == null) {
                try {
                    if (jeditorPane != null) {
                        throw new Exception();
                    }
                    new com.lizardtech.djvu.Document(url);
                } catch (final Throwable exp) {
                    try {
                        jeditorPane = new JEditorPane(url);
                    } catch (final Throwable ignored) {
                    }
                }
            }
            if ((jeditorPane != null)
                    && !(("text/plain".equals(jeditorPane.getContentType())
                    && (jeditorPane.getText().startsWith("AT&T"))))) {
                try {
//          jeditorPane.setEditable(false);
//          use reflection for gcj compatability.
                    final Class[] params = {Boolean.TYPE};
                    final Object[] args = {Boolean.FALSE};
                    JEditorPane.class.getMethod("setEditable", params).invoke(
                            jeditorPane,
                            args);
                    jeditorPane.addHyperlinkListener(
                            new HyperlinkListener() {
                        public void hyperlinkUpdate(final HyperlinkEvent e) {
                            try {
                                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                                    setURL(e.getURL());
                                }
                            } catch (final Throwable exp) {
                                exp.printStackTrace(DjVuOptions.err);
                                System.gc();
                            }
                        }
                    });
                    component = new JScrollPane(jeditorPane);
                    jeditorPane.setCaretPosition(0);
                } catch (Throwable ignored) {
                }
            }

            if (component == null) {
                parameters.put("data", urlString);

                try {
                    final Applet s = (Applet) classApplet.newInstance();
                    s.setStub(this);
                    s.init();
                    component = s;
                    djvuApplet = (com.lizardtech.djview.Applet)s;
                    isDjVu = ((Boolean) isValidDjVuMethod.invoke(s, (Object[]) null)).booleanValue();
                } catch (final Throwable exp) {
                    exp.printStackTrace(DjVuOptions.err);
                    component = null;
                    isDjVu = false;
                }

            }
        } else {
            isDjVu = false;
        }

        removeAll();
        invalidate();
        System.gc();
        setLayout(new BorderLayout());
        add(panel, BorderLayout.NORTH);
        if (component == null) {
            component = new TextArea("No Data Loaded");
        }
        add(component, BorderLayout.CENTER);
        validate();
    }

    /**
     * This applet may also be invoked as a program using javaw.
     *
     * @param args Should contain the target URL.
     */
    public static void main(String[] args) {
        try {
            final DjvuView f = new DjvuView((args.length > 0)
                    ? (args[0])
                    : ".");
            f.run();
            //Thread.sleep(30000L);
        } catch (Throwable exp) {
            exp.printStackTrace(DjVuOptions.err);
            System.exit(1);
        }
    }

    /**
     * Test if the applet active?
     *
     * @return true
     */
    public boolean isActive() {
        return true;
    }

    /**
     * Resize the window.
     *
     * @param width The new window width.
     * @param height The new window height.
     */
    public void appletResize(
            int width,
            int height) {
        setSize(width, height);
    }

    /**
     * Called to show the window.
     */
    public void run() {
        addWindowListener(
                new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                setVisible(false);
                dispose();
            }
        });
        setVisible(true);
    }

    public void movePage(int i) {
        try {
            this.djvuApplet.getDjVuBean().setPage(i);
        } catch (IOException ex) {
        }
    }
    
    public int getPage() {
        try {
            return this.djvuApplet.getDjVuBean().getPage();
        } catch (IOException ex) {
        }
        
        return -1;
    }
}
