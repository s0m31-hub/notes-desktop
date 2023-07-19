package org.nwolfhub;

import javax.swing.*;
import java.awt.event.*;

public class SaveDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonYes;
    private JButton buttonNo;
    private JButton buttonCancel;

    public SaveDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonYes);

        buttonYes.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonNo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onNo();
            }
        });
        buttonCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onNo();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onNo();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        // add your code here
        dispose();
    }

    private void onNo() {
        // add your code here if necessary
        dispose();
    }

    public static void main(String[] args) {
        SaveDialog dialog = new SaveDialog();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
