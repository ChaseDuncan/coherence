package edu.illinois.cs.cogcomp.testPackage;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.AnnotatorService;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Configurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.pipeline.common.PipelineConfigurator;
import edu.illinois.cs.cogcomp.pipeline.main.PipelineFactory;

import java.io.IOException;
import java.util.Properties;

/**
 * A test file to show how to use the {@link AnnotatorService} (here the {@code illinois-nlp-pipeline})
 * to create and annotate {@link TextAnnotation} objects.
 *
 * @author Christos Christodoulopoulos
 */
public class AnnotationTest {
    private static final String text =  "This is a non-tokenized text. It contains 2 sentences.";

    public static void main(String[] args) throws IOException, AnnotatorException {
        /* OPTIONAL STEP:
         * By default the pipeline will load all its annotators which will take up a LOT of memory (~ 8 GB).
         * Use the following properties to disable the annotators you might not need.
         * For now, we're keeping just the POS tagger. */
        Properties nonDefaultProp = new Properties();
        nonDefaultProp.put(PipelineConfigurator.USE_POS.key, Configurator.TRUE);
        nonDefaultProp.put(PipelineConfigurator.USE_LEMMA.key, Configurator.FALSE);
        nonDefaultProp.put(PipelineConfigurator.USE_NER_CONLL.key, Configurator.FALSE);
        nonDefaultProp.put(PipelineConfigurator.USE_NER_ONTONOTES.key, Configurator.FALSE);
        nonDefaultProp.put(PipelineConfigurator.USE_SHALLOW_PARSE.key, Configurator.FALSE);
        nonDefaultProp.put(PipelineConfigurator.USE_STANFORD_DEP.key, Configurator.FALSE);
        nonDefaultProp.put(PipelineConfigurator.USE_STANFORD_PARSE.key, Configurator.FALSE);
        nonDefaultProp.put(PipelineConfigurator.USE_SRL_VERB.key, Configurator.FALSE);
        nonDefaultProp.put(PipelineConfigurator.USE_SRL_NOM.key, Configurator.FALSE);

        // Create the AnnotatorService object
        AnnotatorService annotator = PipelineFactory.buildPipeline(new ResourceManager(nonDefaultProp));

        // Create a new TextAnnotation object. This will tokenize and split the sentences
        // (it will create the TOKENS and SENTENCE views).
        TextAnnotation ta = annotator.createBasicTextAnnotation("corpusID", "textID", text);

        // Add the POS View (run the POS tagger)
        annotator.addView(ta, ViewNames.POS);

        // Accessing the view
        View posView = ta.getView(ViewNames.POS);
        System.out.println(posView);

        // Accessing the Constituents of the View
        for (Constituent c : posView.getConstituents()) {
            System.out.println("Word: " + c.getSurfaceForm() + "\tPOS: " + c.getLabel());
        }
        System.out.println();

        // Accessing the Constituents per sentence
        View sentView = ta.getView(ViewNames.SENTENCE);
        Constituent sent1 = sentView.getConstituents().get(0);
        System.out.println("Sentence 1:");
        for (Constituent c : posView.getConstituentsCovering(sent1)) {
            System.out.println("Word: " + c.getSurfaceForm() + "\tPOS: " + c.getLabel());
        }
    }
}
