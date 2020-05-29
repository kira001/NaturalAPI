package com.hexaTech.repositories;

import com.hexaTech.Main;
import com.hexaTech.interactor.entities.Document;
import com.hexaTech.interactor.repositoriesInterface.RepoGherkinInterface;

import java.io.*;
import java.util.*;

public class RepoGherkin implements RepoGherkinInterface {
    private Document gherkin;

    public RepoGherkin() {
        this.gherkin = new Document();
    }

    public Document getGherkin() {
        return gherkin;
    }

    @Override
    public boolean importDoc(String directory,String document){
        /*String temp;
        JFrame dialog = new JFrame();
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Gherkin file", "scenario");
        chooser.setFileFilter(filter);
        dialog.getContentPane().add(chooser);
        dialog.setAlwaysOnTop(true);
        dialog.setVisible(false);
        dialog.dispose();
        int returnVal = chooser.showOpenDialog(dialog);
        if (returnVal == JFileChooser.APPROVE_OPTION && Files.getFileExtension(chooser.getSelectedFile().getAbsolutePath()).equals("scenario")){
            temp=chooser.getSelectedFile().getAbsolutePath();
        }else{
            temp= "";
        }//if_else
        if(!temp.equalsIgnoreCase("")) {
            gherkins.add(new Document(temp.substring(temp.lastIndexOf("\\")+1),temp));
            saveDoc(".\\BackupGherkin.txt", directory);
            return true;
        }else
            return false;*/
        if(document.equals("") || !existsDoc(document))
            return false;
        System.out.println(document.substring(document.lastIndexOf(".")));
        if(!document.contains(".") || !document.substring(document.lastIndexOf(".")).equalsIgnoreCase(".scenario"))
            return false;
        gherkin=new Document(document.substring(document.lastIndexOf("\\")+1),document);
        saveDoc(".\\BackupGherkin.txt", directory);
        return true;
    }//returnPath

    /**
     * Extrapolates content from a document.
     * @param path string - document's path.
     * @return string - document's content.
     * @throws IOException if the specified document doesn't exist.
     */
    public String getContentFromPath(String path) throws IOException {
        String jarName="/"+path.substring(path.lastIndexOf("\\")+1);
        InputStream input=null;
        BufferedReader br;
        if(RepoGherkin.class.getResourceAsStream(jarName)!=null)
            input = Main.class.getResourceAsStream(jarName);
        if(input==null){
            File file=new File(path);
            br=new BufferedReader(new FileReader(file));
        }else{
            br = new BufferedReader(new InputStreamReader(input));
        }//if_else
        StringBuilder sb = new StringBuilder();
        String line = br.readLine();
        while (line != null) {
            sb.append(line);
            sb.append("\n");
            line = br.readLine();
        }//while
        return sb.toString();
    }//getContentFromPath

    @Override
    public void saveDoc(String title, String directory) {
        String path=gherkin.getPath();
        try {
            // Open given file in append mode.
            File file= new File(directory);
            if (!file.exists())
                file.mkdir();
            BufferedWriter out = new BufferedWriter(
                    new FileWriter(directory + "/" + title));
            out.write(path);
            out.close();
        }catch (IOException e) {
            System.out.println("exception occurred " + e);
        }//try_catch
    }//saveDoc

    /*public BDL extractBDLFromGherkin(String text) throws IOException {
        BDL bdlToReturn =new BDL();
        List<DoubleStruct> result = extract(text);
        bdlToReturn.addSostFromDoubleStruct(result);
        bdlToReturn.addVerbFromDoubleStruct(result);
        bdlToReturn.addPredFromDoubleStruct(result);
        return bdlToReturn;
    }*/

    /**
     * Fills a list with elements found while parsing the given text.
     * @param content string - document's content to analyze.
     * @return List<DoubleStruct> - list of found elements.
     */
    /*private List<DoubleStruct> extract(String content) {
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma");
        StanfordCoreNLP pipeline=new StanfordCoreNLP(props);
        DependencyParser depparser = DependencyParser.loadFromModelFile("edu/stanford/nlp/models/parser/nndep/english_UD.gz");
        List<DoubleStruct> doubleStructs = new ArrayList<>();
        Annotation document = new Annotation(content);
        pipeline.annotate(document);
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        for (CoreMap sentence : sentences) {
            GrammaticalStructure gStruct = depparser.predict(sentence);
            Collection<TypedDependency> dependencies = gStruct.typedDependencies();
            for (TypedDependency dep : dependencies) {
                if (dep.reln().getShortName().equalsIgnoreCase("obj"))
                    doubleStructs.add(new DoubleStruct("obj",dep.gov().lemma()+ " "+ dep.dep().lemma()));
            }//for
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                if (token.tag().contains("VB") || token.tag().contains("NN"))
                    doubleStructs.add(new DoubleStruct(token.tag(), token.lemma()));
            }//for
        }//for
        return doubleStructs;
    }//extract*/

    /**
     * Verifies if the specified document exists.
     * @param path string - path to the document to be searched.
     * @return boolean - true if the document exists, false if not.
     */
    public boolean existsDoc(String path){
        File file=new File(path);
        return file.exists();
    }//existsDoc

    public boolean deleteDoc(String path){
        File temp=new File(path);
        return temp.delete();
    }

    @Override
    public void loadBackup(String directory) throws IOException {
        Scanner s = new Scanner(new File(".\\" + directory + "\\BackupGherkin.txt"));
        String path=s.nextLine();
        while (path!=null){
            gherkin=new Document((path.substring(path.lastIndexOf("\\")+1)), path);
            if(!s.hasNextLine())
                path=null;
            else
                path=s.nextLine();
        }//while
        s.close();
    }//loadBackUp
}