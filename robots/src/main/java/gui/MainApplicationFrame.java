package gui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

import javax.swing.*;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import backup.BackupObj;
import log.Logger;

/**
 * Что требуется сделать:
 * 1. Метод создания меню перегружен функционалом и трудно читается. 
 * Следует разделить его на серию более простых методов (или вообще выделить отдельный класс).
 */
public class MainApplicationFrame extends JFrame implements BackupObj
{
    private final JDesktopPane desktopPane = new JDesktopPane();
    private final String backupFilename = "backup.txt";

    public MainApplicationFrame() {
        //Make the big window be indented 50 pixels from each edge
        //of the screen.
        int inset = 50;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(inset, inset,
                screenSize.width - inset * 2,
                screenSize.height - inset * 2);

        setContentPane(desktopPane);


        LogWindow logWindow = createLogWindow();
        addWindow(logWindow);

        GameWindow gameWindow = new GameWindow();
        gameWindow.setSize(400, 400);
        addWindow(gameWindow);

        GradientWindow gradientWindow = new GradientWindow();
        gradientWindow.setSize(400, 400);
        gradientWindow.setLocation(500, 500);
        addWindow(gradientWindow);

        setJMenuBar(generateMenuBar());
        addClosingConfirmation(this);
    }

    protected int GetConfirmationMessage() {
        return JOptionPane.showConfirmDialog(null,
                "Are you sure you want to exit?", "Exit Message Box",
                JOptionPane.YES_NO_OPTION);
    }

    protected void addClosingConfirmation(JInternalFrame window) {
        window.addInternalFrameListener(new InternalFrameAdapter(){
            public void internalFrameClosing(InternalFrameEvent e) {
                int confirmed = GetConfirmationMessage();
                if (confirmed == JOptionPane.YES_OPTION) {
                    window.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
                } else {
                    window.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
                }
            }
        });
    }

    protected void addClosingConfirmation(JFrame window) {
        window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we)
            {
                int confirmed = GetConfirmationMessage();
                if (confirmed == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        });
    }

    protected LogWindow createLogWindow()
    {
        LogWindow logWindow = new LogWindow(Logger.getDefaultLogSource());
        logWindow.setLocation(10,10);
        logWindow.setSize(300, 800);
        setMinimumSize(logWindow.getSize());
        logWindow.pack();
        Logger.debug("Протокол работает");
        return logWindow;
    }
    
    protected void addWindow(JInternalFrame frame)
    {
        addClosingConfirmation(frame);
        desktopPane.add(frame);
        frame.setVisible(true);
    }

    private void addLookAndFeelMenu(JMenuBar menuBar) {
        JMenu lookAndFeelMenu = new JMenu("Режим отображения");
        lookAndFeelMenu.setMnemonic(KeyEvent.VK_V);
        lookAndFeelMenu.getAccessibleContext().setAccessibleDescription(
                "Управление режимом отображения приложения");

        {
            JMenuItem systemLookAndFeel = new JMenuItem("Системная схема", KeyEvent.VK_S);
            systemLookAndFeel.addActionListener((event) -> {
                int input = JOptionPane.showConfirmDialog(null, "Do you like bacon?");
                setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                this.invalidate();
            });
            lookAndFeelMenu.add(systemLookAndFeel);
        }

        {
            JMenuItem crossplatformLookAndFeel = new JMenuItem("Универсальная схема", KeyEvent.VK_S);
            crossplatformLookAndFeel.addActionListener((event) -> {
                setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                this.invalidate();
            });
            lookAndFeelMenu.add(crossplatformLookAndFeel);
        }
        menuBar.add(lookAndFeelMenu);
    }

    private void addTestMenu(JMenuBar menuBar) {
        JMenu testMenu = new JMenu("Тесты");
        testMenu.setMnemonic(KeyEvent.VK_T);
        testMenu.getAccessibleContext().setAccessibleDescription(
                "Тестовые команды");

        {
            JMenuItem addLogMessageItem = new JMenuItem("Сообщение в лог", KeyEvent.VK_S);
            addLogMessageItem.addActionListener((event) -> {
                Logger.debug("Новая строка");
            });
            testMenu.add(addLogMessageItem);
        }
        menuBar.add(testMenu);
    }

    private void addFileTab(JMenuBar menuBar) {
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_T);
        fileMenu.getAccessibleContext().setAccessibleDescription(
                "File");

        {
            JMenuItem addExitItem = new JMenuItem("Save", KeyEvent.VK_S);
            addExitItem.addActionListener((event) -> {
                try {
                    saveBackup();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            fileMenu.add(addExitItem);
        }

        {
            JMenuItem addExitItem = new JMenuItem("Load", KeyEvent.VK_S);
            addExitItem.addActionListener((event) -> {
                loadBackup();
            });
            fileMenu.add(addExitItem);
        }

        {
            JMenuItem addExitItem = new JMenuItem("Exit", KeyEvent.VK_S);
            addExitItem.addActionListener((event) -> {
                int confirmed = GetConfirmationMessage();
                if (confirmed == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            });
            fileMenu.add(addExitItem);
        }

        menuBar.add(fileMenu);
    }

    private JMenuBar generateMenuBar()
    {
        JMenuBar menuBar = new JMenuBar();
        addLookAndFeelMenu(menuBar);
        addTestMenu(menuBar);
        addFileTab(menuBar);
        return menuBar;
    }
    
    private void setLookAndFeel(String className)
    {
        try
        {
            UIManager.setLookAndFeel(className);
            SwingUtilities.updateComponentTreeUI(this);
        }
        catch (ClassNotFoundException | InstantiationException
            | IllegalAccessException | UnsupportedLookAndFeelException e)
        {
            // just ignore
        }
    }

    @Override
    public String toBackupString() {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < desktopPane.getAllFrames().length; ++i) {
            BackupObj backupWindow = (BackupObj)desktopPane.getAllFrames()[i];
            str.append(backupWindow.toBackupString() + "\n");
        }
        return str.toString();
    }

    @Override
    public void fromBackupString(String backup) {
        String[] data = backup.split("\n");
        JInternalFrame[] frames = desktopPane.getAllFrames();
        for (int i = 0; i < data.length; ++i) {
            for (int j = 0; j < frames.length; ++j) {
                if (data[i].split(" ")[0].equals(frames[j].getTitle())) {
                    BackupObj backupWindow = (BackupObj)frames[j];
                    backupWindow.fromBackupString(data[i]);
                    break;
                }
            }
        }
    }

    public void saveBackup() throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(backupFilename));
        writer.write(toBackupString());
        writer.close();
    }

    public void loadBackup() {
        StringBuilder contentBuilder = new StringBuilder();
        try (Stream<String> stream = Files.lines( Paths.get(backupFilename), StandardCharsets.UTF_8))
        {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        fromBackupString(contentBuilder.toString());
    }
}
