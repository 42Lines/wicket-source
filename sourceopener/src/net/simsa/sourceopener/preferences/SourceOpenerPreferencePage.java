package net.simsa.sourceopener.preferences;

import java.io.IOException;
import java.util.logging.Logger;

import net.simsa.sourceopener.Activator;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By 
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to 
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 */

public class SourceOpenerPreferencePage	extends FieldEditorPreferencePage implements IWorkbenchPreferencePage 
{
	Logger log = Logger.getLogger("SourceOpenerPreferencePage");

	
	public SourceOpenerPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("SourceOpener Preferences");
	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {
		IntegerFieldEditor portField;
		StringFieldEditor passwordField;
		BooleanFieldEditor requirePasswordField;
		IntegerFieldEditor keepCountField;
		
		portField = new IntegerFieldEditor(PreferenceConstants.P_PORT, "&Port", getFieldEditorParent());
		addField(portField);
		
		passwordField = new StringFieldEditor(PreferenceConstants.P_PASSWORD, "Pass&word:", getFieldEditorParent());
		passwordField.getTextControl(getFieldEditorParent()).setEchoChar('*');
		addField(passwordField);
		
		requirePasswordField = new BooleanFieldEditor(PreferenceConstants.P_USEPASSWORD, "&Require password for file-open requests", BooleanFieldEditor.SEPARATE_LABEL, getFieldEditorParent());
		addField(requirePasswordField);
		
		keepCountField = new IntegerFieldEditor(PreferenceConstants.P_KEEP_COUNT, "&Keep how many files in recent history?", getFieldEditorParent());
		addField(keepCountField);
		
		/*
		addField(new DirectoryFieldEditor(PreferenceConstants.P_PATH, 
				"&Directory preference:", getFieldEditorParent()));
		addField(new RadioGroupFieldEditor(
				PreferenceConstants.P_CHOICE,
			"An example of a multiple-choice preference", 1,
			new String[][] { { "&Choice 1", "choice1" }, {
				"C&hoice 2", "choice2" }
		}, getFieldEditorParent()));
		*/
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
}