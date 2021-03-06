package com.hexaTech.adapter.framework;

import com.hexaTech.domain.entity.DoubleStruct;
import com.hexaTech.domain.entity.Gherkin;
import com.hexaTech.domain.entity.Parameter;
import com.hexaTech.domain.entity.StructureBAL;
import com.hexaTech.domain.port.out.framework.TextsParsingInterface;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.util.CoreMap;
import org.apache.commons.text.CaseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

@Component
public class StanfordNLP implements TextsParsingInterface {
    private final StanfordCoreNLP pipeline;

    private final DependencyParser depparser;

    private final Properties props;

    @Autowired
    public StanfordNLP() {
        this.props = new Properties();
        this.props.put("annotators", "tokenize, ssplit, pos, lemma");
        this.pipeline = new StanfordCoreNLP(this.props);
        this.depparser = DependencyParser.loadFromModelFile("edu/stanford/nlp/models/parser/nndep/english_UD.gz");
    }

    /**
     * Fills a list with elements found while parsing the given text.
     *
     * @param content string - document's content to analyze.
     * @return List<DoubleStruct> - list of found elements.
     */
    public List<DoubleStruct> extractBDLFromText(String content) {
        List<DoubleStruct> doubleStructs = new ArrayList<>();
        Annotation document = new Annotation(content);
        pipeline.annotate(document);
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        for (CoreMap sentence : sentences) {
            GrammaticalStructure gStruct = depparser.predict(sentence);
            Collection<TypedDependency> dependencies = gStruct.typedDependencies();
            for (TypedDependency dep : dependencies) {
                if (dep.reln().getShortName().equalsIgnoreCase("obj"))
                    doubleStructs.add(new DoubleStruct("obj", dep.gov().lemma()+" "+dep.dep().lemma()));
            }//for
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                if (token.tag().contains("VB") || token.tag().contains("NN"))
                    if (isACommonVerb(token.lemma()))
                        doubleStructs.add(new DoubleStruct(token.tag(), token.lemma()));
            }//for
        }//for
        return doubleStructs;
    }//extractBDLFromText

    public List<StructureBAL> extractBOFromText(String content) {
        List<StructureBAL> structureBALList=new ArrayList<>();
        Annotation document = new Annotation(content);
        pipeline.annotate(document);
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        for (CoreMap sentence : sentences) {
            GrammaticalStructure gStruct = depparser.predict(sentence);
            Collection<TypedDependency> dependencies = gStruct.typedDependencies();
            for (TypedDependency dep : dependencies) {
                if (dep.reln().getShortName().equalsIgnoreCase("obj")) {
                    if(dep.gov().lemma().equalsIgnoreCase("have"))
                        structureBALList.add(extractBO(props,sentence));
                }//if
            }//for
        }//for
        return structureBALList;
    }//extractBOFromText

    private StructureBAL extractBO(Properties properties, CoreMap sentence){
        //Sentence phrase=new Sentence(sentence.toString());
        //SemanticGraph semanticGraph=phrase.dependencyGraph(properties, SemanticGraphFactory.Mode.ENHANCED);
        //Collection<TypedDependency> dependencies = semanticGraph.typedDependencies();
        GrammaticalStructure gStruct = depparser.predict(sentence);
        Collection<TypedDependency> dependencies = gStruct.typedDependencies();
        StructureBAL bo=new StructureBAL();
        String directObj="";
        for (TypedDependency dep : dependencies) {
            if(dep.reln().getShortName().equalsIgnoreCase("nsubj"))
                bo.setName(getLemma(dep.dep().toString()).substring(0,1).toUpperCase()+getLemma(dep.dep().toString()).substring(1));
            if(dep.reln().getShortName().equalsIgnoreCase("obj")) {
                bo.setParameters(new Parameter("", getLemma(dep.dep().toString()), "string"));
                directObj = dep.dep().lemma();
            }
            if(dep.reln().getShortName().equalsIgnoreCase("conj") && dep.gov().lemma().equalsIgnoreCase(directObj)) {
                bo.setParameters(new Parameter("", getLemma(dep.dep().toString()), "string"));
            }

        }//for
        return bo;
    }//extractBO

    private String getLemma(String word){
        return word.substring(0,word.indexOf("/"));
    }

    public List<Gherkin> extractFromGherkin(String text) {
        List<Gherkin> toRet = new ArrayList<>();
        //String[] gherkinSplit = text.split("[\n]+[\n]");
        String[] gherkinSplit = text.split("Scenario:");
        String restore = "Scenario:";
        ArrayList<String> gherkinSplitted = new ArrayList<>();
        for (int i = 0; i < gherkinSplit.length - 1 ; i++) {
            gherkinSplitted.add(restore+gherkinSplit[i+1]);
        }
        for (String scenario : gherkinSplitted) {
            String[] arr = scenario.split("[\n]+");
            Gherkin toAdd = new Gherkin();
            String sentinel = "";
            toAdd.setDescription(scenario.replace("\n", "---"));
            for (String str : arr) {
                CoreDocument documents = new CoreDocument(str);
                pipeline.annotate(documents);
                StringBuilder builder = new StringBuilder();
                String firstToken="";
                if(!documents.sentences().isEmpty()) {
                    firstToken = documents.sentences().get(0).tokensAsStrings().get(0);
                    if (firstToken.equalsIgnoreCase("AND")) {
                        firstToken = sentinel;
                    }//if
                }
                Annotation document = new Annotation(str);
                pipeline.annotate(document);
                GrammaticalStructure gStruct = depparser.predict(document);
                Collection<TypedDependency> dependencies = gStruct.typedDependencies();
                switch (firstToken.toLowerCase()) {
                    case ("given"):
                        toAdd.setGiven("given");
                        sentinel = "given";
                        break;
                    case ("when"):
                        StringBuilder builder2 = new StringBuilder();
                        String directObj="";
                        for (TypedDependency dep : dependencies) {
                            if (dep.reln().getShortName().equalsIgnoreCase("root"))
                                builder2.append(dep.dep().lemma()).append(" ");
                            if (dep.reln().getShortName().equalsIgnoreCase("obj")) {
                                directObj=dep.dep().lemma();
                                String tmp=dep.dep().lemma();
                                builder2.append(dep.dep().lemma()).append(" ");
                                toAdd.getWhen().add(tmp);
                            }
                            if (dep.reln().getShortName().equalsIgnoreCase("conj") && dep.gov().lemma().equalsIgnoreCase(directObj)) {
                                String tmp=dep.dep().lemma();
                                builder2.append(dep.dep().lemma()).append(" ");
                                toAdd.getWhen().add(tmp);
                            }
                        }//for
                        //builder2 per il titolo del metodo
                        //builder per i parametri
                        toAdd.setScenario(CaseUtils.toCamelCase(builder2.toString(),false, ' '));
                        sentinel = "when";
                        break;
                    case ("then"):
                        for (TypedDependency dep : dependencies) {
                            if (dep.reln().getShortName().equalsIgnoreCase("obj"))
                                builder.append(dep.dep().lemma()).append(" ");
                        }//for
                        toAdd.setThen(builder.toString());
                        sentinel = "then";
                        break;
                }//switch
            }//for
            toRet.add(toAdd);
        }//for
        return toRet;
    }//extractFromGherkin

    public List<DoubleStruct> extractBDLFromGherkin(String content){
        List<DoubleStruct> doubleStructs = new ArrayList<>();
        Annotation document = new Annotation(content);
        pipeline.annotate(document);
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        for (CoreMap sentence : sentences) {
            GrammaticalStructure gStruct = depparser.predict(sentence);
            Collection<TypedDependency> dependencies = gStruct.typedDependencies();
            for (TypedDependency dep : dependencies) {
                if (dep.reln().getShortName().equalsIgnoreCase("obj") &&
                        !dep.dep().lemma().equalsIgnoreCase("feature") &&
                        !dep.dep().lemma().equalsIgnoreCase("scenario"))
                    doubleStructs.add(new DoubleStruct("obj", dep.gov().lemma() + " " + dep.dep().lemma()));
            }//for
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                if (token.tag().contains("VB") || token.tag().contains("NN") &&
                        !token.lemma().equalsIgnoreCase("feature")&&
                        !token.lemma().equalsIgnoreCase("scenario"))
                    if (isACommonVerb(token.lemma()))
                        doubleStructs.add(new DoubleStruct(token.tag(), token.lemma()));
            }//for
        }//for
        return doubleStructs;

    }

    private boolean isACommonVerb(String verb) {
        String[] commonVerbs = {"be", "have", "do", "say", "go", "get", "make", "know", "think",
                "take", "see", "come", "want", "look", "use", "find", "give", "tell",
                "work"};
        for (String commonV : commonVerbs) {
            if (commonV.equalsIgnoreCase(verb))
                return false;
        }
        return true;
    }
}//StanfordNLP