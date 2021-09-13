package gin.edit.modifynode;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Optional;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.ImportDeclaration;

import gin.SourceFile;
import gin.SourceFileTree;
import gin.edit.Edit;

/**
 * A mutation operator that replaces compatible Java Collections.
 * 
 * It supports List types only. To update simply update the REPLACEMENT and IMPORT_DECLARATIONS maps.
 * It works for new collection declarations, e.g., List smth = new X : X can be replaced
 * Note: It will fail if the type is declared before new is called, e.g. ArrayList smth = new 
 */
public class CollectionReplacement extends ModifyNodeEdit {
    public String targetFilename;
    private final int targetNode;
    private final SimpleName source;
    private final SimpleName replacement;
   
    /** declares allowable replacements for each Collection class 
     * currently supports List replacements only
     */
    private static final Map<SimpleName, List<SimpleName>> REPLACEMENTS = new LinkedHashMap<>();
    static {

        // Implement List Replacements

        REPLACEMENTS.put(new SimpleName("ArrayList"), Arrays.asList(new SimpleName("LinkedList"), new SimpleName("CopyOnWriteArrayList")));
        REPLACEMENTS.put(new SimpleName("CopyOnWriteArrayList"), Arrays.asList(new SimpleName("ArrayList"), new SimpleName("LinkedList")));
        REPLACEMENTS.put(new SimpleName("LinkedList"), Arrays.asList(new SimpleName("ArrayList"), new SimpleName("CopyOnWriteArrayList")));

        // Implement Map Replacements

        REPLACEMENTS.put(new SimpleName("HashMap"), Arrays.asList(new SimpleName("LinkedHashMap")));
        REPLACEMENTS.put(new SimpleName("LinkedHashMap"), Arrays.asList(new SimpleName("HashMap")));

        // Implement Set Replacements

        REPLACEMENTS.put(new SimpleName("HashSet"), Arrays.asList(new SimpleName("LinkedHashSet")));
        REPLACEMENTS.put(new SimpleName("LinkedHashSet"), Arrays.asList(new SimpleName("HashSet")));

    }
    
    /** declares import statements for each Collection class 
     * currently supports List replacements only
     */
    private static final Map<SimpleName, List<ImportDeclaration>> IMPORT_DECLARATIONS = new LinkedHashMap<>();
    static {

        // Declarations need to be declared in reverse
        // Implement List Declaration

        IMPORT_DECLARATIONS.put(new SimpleName("ArrayList"), Arrays.asList(new ImportDeclaration("java.util.ArrayList", false, false), new ImportDeclaration("java.util", false, true)));
        IMPORT_DECLARATIONS.put(new SimpleName("CopyOnWriteArrayList"), Arrays.asList(new ImportDeclaration("java.util.concurrent.CopyOnWriteArrayList", false, false), new ImportDeclaration("java.util.concurrent", false, true), new ImportDeclaration("java.util", false, true)));
        IMPORT_DECLARATIONS.put(new SimpleName("LinkedList"), Arrays.asList(new ImportDeclaration("java.util.LinkedList", false, false), new ImportDeclaration("java.util", false, true)));

        // Implement Map Declarations

        IMPORT_DECLARATIONS.put(new SimpleName("HashMap"), Arrays.asList(new ImportDeclaration("java.util.HashMap", false, false), new ImportDeclaration("java.util", false, true)));
        IMPORT_DECLARATIONS.put(new SimpleName("LinkedHashMap"), Arrays.asList(new ImportDeclaration("java.util.LinkedHashMap", false, false), new ImportDeclaration("java.util", false, true)));

        // Implement Set Declarations

        IMPORT_DECLARATIONS.put(new SimpleName("HashSet"), Arrays.asList(new ImportDeclaration("java.util.HashSet", false, false), new ImportDeclaration("java.util", false, true)));
        IMPORT_DECLARATIONS.put(new SimpleName("LinkedHashSet"), Arrays.asList(new ImportDeclaration("java.util.LinkedHashSet", false, false), new ImportDeclaration("java.util", false, true)));

    }
    
    /**
     * @param sourceFile to create an edit for
     * @param rng random number generator, used to choose the target statements
     * @throws NoApplicableNodesException if sourcefile doesn't contain any unary operators
     */
    public CollectionReplacement(SourceFile sourceFile, Random rng) throws NoApplicableNodesException {
        SourceFileTree sf = (SourceFileTree)sourceFile;
        this.targetNode = sf.getRandomNodeID(true, ObjectCreationExpr.class, rng);
        
        if (this.targetNode < 0) {
            throw new NoApplicableNodesException();
        }
       
	ClassOrInterfaceType tmpsource = ((ObjectCreationExpr)sf.getNode(this.targetNode)).getType();
        this.source = tmpsource.getName();
        this.replacement = chooseRandomReplacement(source, rng);
        this.targetFilename = sourceFile.getFilename();
    }
    
    /** instantiates CollectionReplacements */
    public CollectionReplacement(String sourceFileName, int targetNodeID, SimpleName sourceSimpleName, SimpleName replacementSimpleName) {
        this.targetNode = targetNodeID;
        this.source = sourceSimpleName;
        this.replacement = replacementSimpleName;
        this.targetFilename = sourceFileName;
    }
    
    /** applies the patch */
    @Override
    public SourceFile apply(SourceFile sourceFile) {
        SourceFileTree sf = (SourceFileTree)sourceFile;
        Node node = sf.getNode(targetNode);
        
        if (node == null) {
            return sf; // targeting a deleted location just does nothing.
        } else {
            ClassOrInterfaceType substitute = ((ObjectCreationExpr)node).getType();
	    substitute.setName(replacement);
	    ((ObjectCreationExpr)node).setType(substitute);

            sf = sf.replaceNode(this.targetNode, node);

	    List<ImportDeclaration> impDec = sf.getImportDeclarations();

	    boolean check = false;
	    for (ImportDeclaration iMd : IMPORT_DECLARATIONS.get(replacement)) {
		if (impDec.contains(iMd)) {
		    check = true;
		    break;
		}
	    }

	    if (!check) {
		ImportDeclaration iMd = IMPORT_DECLARATIONS.get(replacement).get(0);
		sf.addImport(iMd);
	    } 
	    
            
            return sf;
        }
    }
   
    /** selects a replacements from allowable ones */ 
    private static SimpleName chooseRandomReplacement(SimpleName original, Random r) {

	SimpleName replacement;
        List<SimpleName> l = REPLACEMENTS.get(original);
	replacement = l.get(r.nextInt(l.size()));
        
        return replacement;
    }

    @Override
    public EditType getEditType() {
        return EditType.MODIFY_STATEMENT;
    }
    
    @Override
    public String toString() {
        return super.toString() + " " + targetFilename + ":" + targetNode + " " + source + " -> " + replacement + "";
    }
    
    public static Edit fromString(String description) {
        String tokens[] = description.split("\\s+");
        String sourceTokens[] = tokens[1].split(":");
        String sourceFile = sourceTokens[0];
        int targetNodeID = Integer.parseInt(sourceTokens[1]);
        SimpleName sourceSimpleName = new SimpleName(tokens[2]);
        SimpleName replacementSimpleName = new SimpleName(tokens[4]);
        
        return new CollectionReplacement(sourceFile, targetNodeID, sourceSimpleName, replacementSimpleName);
    }
}
