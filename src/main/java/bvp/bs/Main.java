package bvp.bs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    public static Kernel32.SYSTEM_POWER_STATUS batteryStatus = new Kernel32.SYSTEM_POWER_STATUS();

    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        } catch (InstantiationException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }

        UIManager.put("swing.boldMetal", Boolean.FALSE);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                createAndShowGUI();
            }
        });
    }

    private static void createAndShowGUI() {
        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray is not supported");
            return;
        }
        final PopupMenu popup = new PopupMenu();
        final TrayIcon trayIcon = new TrayIcon(createImage("/images/battery_status.gif", "tray icon"));
        final SystemTray tray = SystemTray.getSystemTray();

        MenuItem aboutItem = new MenuItem("О программе");
        MenuItem exitItem = new MenuItem("Выход");

        popup.add(aboutItem);
        popup.addSeparator();
        popup.add(exitItem);

        trayIcon.setPopupMenu(popup);
        trayIcon.setImageAutoSize(true);

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.out.println("TrayIcon could'n be added");
            return;
        }

        aboutItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, "Программа разработана на основе информации из Лайфхакера.\n"
                        + "Статья http://lifehacker.ru/2013/10/05/kak-pravilno-zaryazhat-noutbuk/");
            }
        });


        exitItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tray.remove(trayIcon);
                System.exit(0);
            }
        });

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate
                (new Runnable() {
                    public void run() {
                        Kernel32.INSTANCE.GetSystemPowerStatus(batteryStatus);

                        if ("Online".equals(batteryStatus.getACLineStatusString())) {
                            trayIcon.setToolTip("Батарея заряжается\n"
                                    + "Уровень заряда " + batteryStatus.getBatteryLifePercent() + "%");
                            if (Integer.parseInt(batteryStatus.getBatteryLifePercent()) >= 80) {
                                trayIcon.displayMessage("Battery status", "Уровень заряда "
                                        + batteryStatus.getBatteryLifePercent() + "%\n"
                                        + "Теперь можно отключить зарядку", TrayIcon.MessageType.INFO);
                            }
                        } else {
                            trayIcon.setToolTip("Батарея разряжается\n"
                                    + "Уровень заряда " + batteryStatus.getBatteryLifePercent() + "%");
                            if (Integer.parseInt(batteryStatus.getBatteryLifePercent()) <= 40) {
                                trayIcon.displayMessage("Battery status", "Уровень заряда "
                                        + batteryStatus.getBatteryLifePercent() + "%\n"
                                        + "Пора подключить зарядку", TrayIcon.MessageType.INFO);
                            }
                        }
                    }
                }, 0, 5, TimeUnit.SECONDS);
    }

    protected static Image createImage(String path, String description) {
        URL imageURL = Main.class.getResource(path);

        if (imageURL == null) {
            System.err.println("Resource not found: " + path);
            return null;
        } else {
            return (new ImageIcon(imageURL, description)).getImage();
        }
    }
}
