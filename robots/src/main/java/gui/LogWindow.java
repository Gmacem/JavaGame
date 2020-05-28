package gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.TextArea;
import java.beans.PropertyVetoException;

import javax.swing.*;

import log.LogChangeListener;
import log.LogEntry;
import log.LogWindowSource;

import backup.BackupObj;

public class LogWindow extends JInternalFrame implements LogChangeListener, BackupObj
{
    private LogWindowSource m_logSource;
    private TextArea m_logContent;

    public LogWindow(LogWindowSource logSource) 
    {
        super("Протокол_работы", true, true, true, true);
        m_logSource = logSource;
        m_logSource.registerListener(this);
        m_logContent = new TextArea("");
        m_logContent.setSize(200, 500);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(m_logContent, BorderLayout.CENTER);
        getContentPane().add(panel);
        pack();
        updateLogContent();
    }

    private void updateLogContent()
    {
        StringBuilder content = new StringBuilder();
        for (LogEntry entry : m_logSource.all())
        {
            content.append(entry.getMessage()).append("\n");
        }
        m_logContent.setText(content.toString());
        m_logContent.invalidate();
    }

    @Override
    public void onLogChanged()
    {
        EventQueue.invokeLater(this::updateLogContent);
    }

    @Override
    public String toBackupString() {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("%s %d %d %d %d %b %b",
                getTitle(), getWidth(), getHeight(), getX(), getY(), isClosed(), isIcon()));
        if (isIcon()) {
            JDesktopIcon icon = getDesktopIcon();
            builder.append(String.format(" %d %d %d %d",
                    icon.getWidth(), icon.getHeight(), icon.getX(), icon.getY()));
        }
        return builder.toString();
    }

    @Override
    public void fromBackupString(String backup) {
        String[] tokens = backup.split(" ");
        setTitle(tokens[0]);
        setSize(Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]));
        setLocation(Integer.parseInt(tokens[3]), Integer.parseInt(tokens[4]));
        if (Boolean.parseBoolean(tokens[5])) {
            dispose();
        }
        try {
            setIcon(Boolean.parseBoolean(tokens[6]));
        } catch (PropertyVetoException e) {
            e.printStackTrace();
        }
        if (isIcon()) {
            getDesktopIcon().setSize(Integer.parseInt(tokens[7]), Integer.parseInt(tokens[8]));
            getDesktopIcon().setLocation(Integer.parseInt(tokens[8]), Integer.parseInt(tokens[9]));
            setSize(getMinimumSize());
        }
    }
}
