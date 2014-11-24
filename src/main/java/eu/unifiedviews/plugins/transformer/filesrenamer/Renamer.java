package eu.unifiedviews.plugins.transformer.filesrenamer;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dataunit.fileshelper.FilesHelper;
import eu.unifiedviews.helpers.dataunit.virtualpathhelper.VirtualPathHelper;
import eu.unifiedviews.helpers.dataunit.virtualpathhelper.VirtualPathHelpers;
import eu.unifiedviews.helpers.dpu.NonConfigurableBase;

@DPU.AsTransformer
public class Renamer extends NonConfigurableBase {

    private static final Logger LOG = LoggerFactory.getLogger(Renamer.class);

    @DataUnit.AsInput(name = "filesInput")
    public FilesDataUnit filesInput;

    @DataUnit.AsOutput(name = "filesOutput")
    public WritableFilesDataUnit filesOutput;

    public Renamer() {
//        super(FilesToFilesRenameTransformerConfig.class);
    }

//    @Override
//    public AbstractConfigDialog<FilesToFilesRenameTransformerConfig> getConfigurationDialog() {
//        return new FilesToFilesRenameTransformerConfigDialog();
//    }

    @Override
    public void execute(DPUContext dpuContext) throws DPUException {
        //check that XSLT is available

        String shortMessage = this.getClass().getSimpleName() + " starting.";
//        String longMessage = String.valueOf(config);
//        dpuContext.sendMessage(MessageType.INFO, shortMessage, longMessage);
        dpuContext.sendMessage(DPUContext.MessageType.INFO, shortMessage, "");

        final Iterator<FilesDataUnit.Entry> filesIteration;
        try {
            filesIteration = FilesHelper.getFiles(filesInput).iterator();
        } catch (DataUnitException ex) {
            throw new DPUException("Could not obtain filesInput", ex);
        }
        long filesSuccessfulCount = 0L;
        long index = 0L;
        boolean shouldContinue = !dpuContext.canceled();

        VirtualPathHelper virtualPathHelperInput = VirtualPathHelpers.create(filesInput);
        VirtualPathHelper virtualPathHelperOutput = VirtualPathHelpers.create(filesOutput);
        try {

            while (shouldContinue && filesIteration.hasNext()) {
                FilesDataUnit.Entry entry;
                try {
                    entry = filesIteration.next();
                    index++;

                   
                    final String newSymbolicName = entry.getSymbolicName() + ".ttl";
                    

                        //Get the current date:
						DateFormat dateFormat = new SimpleDateFormat("yyMMdd");
						Date date = new Date();
                   
						/*						
						 *please consider  the following rules
						[N].ttl             changes extension to ttl
						[N].[E].ttl        add extension .ttl
						abc_[N].[E]       prefixes filename with abc_
						[N]-[YMD].[E] changes filename into form of name-date.extension

                        */
						
						
                 final String newVirtualPath
                         = virtualPathHelperInput.getVirtualPath(entry.getSymbolicName())+"_"+ dateFormat.format(date)+ ".ttl";

                 
                 
                     String checkName= " new name:   " +  virtualPathHelperInput.getVirtualPath(entry.getSymbolicName())+"_"+ dateFormat.format(date)+ ".ttl";
                     dpuContext.sendMessage(DPUContext.MessageType.INFO, checkName, "");

                    //final String newVirtualPath = virtualPathHelperInput.getVirtualPath(entry.getSymbolicName()) + ".ttl";

                    filesOutput.addExistingFile(newSymbolicName, entry.getFileURIString());
                    virtualPathHelperOutput.setVirtualPath(newSymbolicName, newVirtualPath);

                    filesSuccessfulCount++;
                } catch (DataUnitException ex) {
                    dpuContext.sendMessage(
                            DPUContext.MessageType.ERROR,
                            "DataUnit exception.",
                            "",
                            ex);
                }

                shouldContinue = !dpuContext.canceled();
            }
        } finally {
            try {
                virtualPathHelperInput.close();
                virtualPathHelperOutput.close();
            } catch (DataUnitException ex) {
                LOG.warn("Error closing filesInput", ex);
            }
        }
        String message = String.format("Processed %d/%d", filesSuccessfulCount, index);
        dpuContext.sendMessage(filesSuccessfulCount < index ? DPUContext.MessageType.WARNING : DPUContext.MessageType.INFO, message);
    }

    public static String appendNumber(long number) {
        String value = String.valueOf(number);
        if (value.length() > 1) {
            // Check for special case: 11 - 13 are all "th".
            // So if the second to last digit is 1, it is "th".
            char secondToLastDigit = value.charAt(value.length() - 2);
            if (secondToLastDigit == '1') {
                return value + "th";
            }
        }
        char lastDigit = value.charAt(value.length() - 1);
        switch (lastDigit) {
            case '1':
                return value + "st";
            case '2':
                return value + "nd";
            case '3':
                return value + "rd";
            default:
                return value + "th";
        }
    }
}
