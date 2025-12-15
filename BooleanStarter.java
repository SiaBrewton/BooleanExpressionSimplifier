import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;
/**
* CSC 204 – Boolean Simplifier Starter Program
*
* This is ONLY a starter.
* Students MUST:
* - Implement all Boolean algebra rules
* - Add simplification logic
* - Add step-by-step tracing
* - Add more tests
*
* Provided:
* - Basic AST node classes
* - Simple parser (char[] based)
* - Basic simplify() structure (EMPTY)
*
* Not provided:
* - ALL Boolean rules (students write these)
* - Full simplifier
* - Full step trace system
*/
public class BooleanStarter {
// ======== AST NODES ========
interface Node {}
static class Var implements Node {
String name;
Var(String n) { name = n; }
public String toString() { return name; }
}
static class Const implements Node {
boolean value;
Const(boolean v) { value = v; }
public String toString() { return value ? "1" : "0"; }
}
static class Not implements Node {
Node x;
Not(Node x) { this.x = x; }
public String toString() {
boolean simple = (x instanceof Var) || (x instanceof Const);
return (simple ? x.toString() : "(" + x + ")") + "'";
}
}
static class And implements Node {
Node left, right;

    And(Node l, Node r) {
        left = l;
        right = r;
    }
 public String toString() {
    String L = (left instanceof Or ? "(" + left + ")" : left.toString());
    String R = (right instanceof Or ? "(" + right + ")" : right.toString());
    return L + R;
    }
}
static class Or implements Node {
Node left, right;
Or(Node l, Node r) { left = l; right = r; }
public String toString() {
return left.toString() + " + " + right.toString();
}
}
// ======== SIMPLE PARSER (char[] based) ========
static class Parser {
char[] s;
int i = 0;
Parser(String input) {
s = input.replaceAll("\\s+", "").toCharArray();
}
boolean eof() { return i >= s.length; }
char peek() { return s[i]; }
char next() { return s[i++]; }
Node parse() { return expr(); }
// expr := term ('+' term)*
Node expr() {
Node left = term();
while (!eof() && peek() == '+') {
next();
Node right = term();
left = new Or(left, right);
}
return left;
}
boolean startsFactor(char c) {
    return isVar(c) || c=='(' || c=='0' || c=='1';
}

// term := factor factor* (implicit AND)
Node term() {
Node left = factor();
while (!eof() && startsFactor(peek())) {
    Node right = factor();
    left = new And(left, right);
}
return left;
}
// factor := base ('\'' )*
Node factor() {
Node b = base();
while (!eof() && peek()=='\'') {
next();
b = new Not(b);
}
return b;
}

Node base() {
char c = peek();
if (isVar(c)) {
next();
return new Var("" + c);
}
if (c == '0') { next(); return new Const(false); }
if (c == '1') { next(); return new Const(true); }
if (c == '(') {
next();
Node e = expr();
if (peek() != ')') throw new RuntimeException("Missing ')'");
next(); // expect ')'
return e;
}
throw new RuntimeException("Unexpected: " + c);
}
boolean isVar(char c) {
return c >= 'A' && c <= 'Z';
}
}

// ======== SIMPLIFIER (STUDENT WORK) ========
// STUDENTS MUST:
// - Implement ALL Boolean rules from lecture
// - Add recursive simplification
// - Implement:
// * Identity laws
// * Null laws
// * Complement laws
// * Idempotent laws
// * Absorption
// * Involution
// - Return simplified Node
// - Add step tracing for each rule
//
// For now, return input unchanged.


//Tracing 
//store step-by-step of simplification
static StringBuilder tracelog = new StringBuilder();
 static void tracer(String s) {
  tracelog.append(s).append("\n"); 
  }
// Rule countsteps in the trace log
static int ruleCounter = 1;

static void tracer(String ruleCode, String description) {
    tracelog.append(ruleCounter++) //step #
            .append(". ")
            .append(ruleCode)// rule 
            .append(" ")
            .append(description) //Description of the simplification
            .append("\n");
    }
    
//simplifies until  "fixed point"
    static Node NoSimplification(Node root) {
    ruleCounter = 1;  // reset step #
    tracelog.setLength(0); // Clear log

    while (true) {
        String before = root.toString(); //before simplification 
        root = simplify(root);
        String after = root.toString(); // after simplification   

        if (before.equals(after)) {
            System.out.println("\nTrace Log:\n" + tracelog);
            //if expression didnt change 
            System.out.println("No further simplification — expression is in fixed point under rules R1–R12, DM1–DM2.");
            System.out.println("Result: " + root);
            return root; // return new expression 
        }
        // otherwise loop to try further simplification passes
    }
}
  
 //  simplifies the node until fixed point
static Node simplify(Node n) {
    Node old;
    do {
        old = n;  // previous expression state 
        n = simplifyStep(n); // one simplified pass
    } while (!n.toString().equals(old.toString()));
    return n; // return full simplied expression 
}
    
//Simplifier  
//Single step   
static Node simplifyStep(Node n) {
    //end simplification 
   if (n instanceof Var || n instanceof Const) 
   return n;

    if (n instanceof Not) { //NOT
        Not z = (Not)n;
        Node inner = simplifyStep(z.x);// simplify inner expression first 
        return simplifyNot(inner);
    }
    if (n instanceof And) { //AND
       And Anode = (And)n;
            Node L = simplify(Anode.left);//  simplify left 
            Node R = simplify(Anode.right);//  simplify right 
            return simplifyAnd(L, R);
        }
     if (n instanceof Or) { //OR
        Or Onode = (Or)n;
            Node L = simplify(Onode.left);//  simplify left 
            Node R = simplify(Onode.right);//  simplify right 
            return simplifyOr(L, R);           
                }

    return n; 
}
    // Checks if two nodes are complements (A and A')  
    //Complement Law check  
    static boolean isComplement(Node a, Node b){
    return (a instanceof Not && ((Not)a).x.toString().equals(b.toString())) ||
           (b instanceof Not && ((Not)b).x.toString().equals(a.toString()));
}


// Simplify NOT 
    static Node simplifyNot(Node x) {
    
        // Rule 6 -Involution Law (A')' => A 
        if (x instanceof Not) {  // If x is already a Not node, the inside expression is returned directly.
            Node inner = ((Not)x).x;
            tracer("R6", "Involution ((A')' → A): (" + x + ") → " + inner);
            return inner;// simplified result
        }
       
        // Rule7 - NOT constant:
        // NOT 0 = 1, NOT 1 = 0
        if (x instanceof Const) {
            Const result = new Const(!((Const)x).value);
            tracer("R7", "Negation of constant: " + x + " → " + result);
            return result;
        }
        //Rule DM1 for AND -  De morgans (AB)' → A' + B'
        // apply NOT to each subterm of the AND
        if (x instanceof And) {
            And Anode = (And)x;
            Node result = new Or(simplifyNot(Anode.left), simplifyNot(Anode.right));
            tracer("R8", "De Morgan (AB)' → A' + B': " + x + " → " + result);
            return simplify(result);
        }
        
        // Rule DM2 for OR - De morgans pt.2 (A+B)' → A'B'
        // apply NOT to each subterm of the OR
         if (x instanceof Or) {
            Or o = (Or)x;
            Node result = new And(simplifyNot(o.left), simplifyNot(o.right));
            tracer("R9", "De Morgan (A+B)' → A'B': " + x + " → " + result);
            return simplify(result); //simplify the result
        }
        
        // Otherwise keep Not(x)
        return new Not(x);
    }
    


    //Linked List 
    //Supports Absorption and Distributive laws 
    //flatten And : (A * (B * C)) → [A, B, C]
    static List<Node> flattenAnd(Node n) {
        List<Node> list = new ArrayList<>();
        // If the node is an AND operation,flatten its left and right children
        if (n instanceof And) {
            list.addAll(flattenAnd(((And)n).left));
            list.addAll(flattenAnd(((And)n).right));
        } else {
            // If the node is not an AND add it as-is
            list.add(n);
        }
        return list; // Return list of AND terms
    }
    
   // supports simplification laws like Absorption, Idempotent, and Complement Laws
   // flatten Or (A + (B + C)) → [A, B, C]
    static List<Node> flattenOr(Node n) { //ist to hold all OR terms
        List<Node> list = new ArrayList<>();
        // If the node is an OR operation, flatten its left and right children
        if (n instanceof Or) {
            list.addAll(flattenOr(((Or)n).left));
            list.addAll(flattenOr(((Or)n).right));
        } else {
            list.add(n);
        }
        return list; ///Return list of OR terms
    }
    
   //Simplify AND with boolean algebra
   
   //simplify both left and right sections
   // simplify inside nested expressions first
   static Node simplifyAnd(Node L, Node R) {
          L = simplify(L);
          R = simplify(R);
          
     // Rule 2 - Null law (A*0 = 0) 
     // If 0 is present, the whole AND is 0
        if ((L instanceof Const && !((Const)L).value) || (R instanceof Const && !((Const)R).value)) {
            tracer("R2", "Null Law: 0 in AND → 0");
            return new Const(false);
        }

    // Rule 3 - Identity law (A*1 = A)
    // Multiplying by 1 results in same answer 
        if (L instanceof Const && ((Const)L).value) return R;
        if (R instanceof Const && ((Const)R).value) return L;

    // Rule 4 - Complement Law (A*A' = 0)
    // If one side is the negation of the other, result is 0
       if (isComplement(L,R)) {
           tracer("R4", "Complement Law: " + L + " * " + R + " → 0");
           return new Const(false);
        }

   // Rule R1 : Idempotent Law (A * A = A)
   // AND of same term = same term
      if (L.toString().equals(R.toString())) {
         tracer("R1", "Idempotent Law: " + L + " * " + R + " → " + L);
         return L;
        }

   // Rule 5: Absorption Law : A * (A + B) = A
   // If one side is OR and same variable on other side, absorb
      if (L instanceof Or) {
          Or Onode = (Or)L;
            if (Onode.left.toString().equals(R.toString()) || Onode.right.toString().equals(R.toString())) {
               tracer("R5", "Absorption: (" + L + ") * " + R + " → " + R);
               return R;
            }
        }
    if (R instanceof Or) {
         Or Onode = (Or)R;
            if (Onode.left.toString().equals(L.toString()) || Onode.right.toString().equals(L.toString())) {
             tracer("R5", "Absorption: " + L + " * (" + R + ") → " + L);
              return L;
            }
        }

    // Rule6/7 : Distributive Law (A*(B+C) = A*B + A*C)
    // If either side is an OR, distribute AND over OR:
       if (L instanceof Or || R instanceof Or) {
           List<Node> leftTerms = L instanceof Or ? flattenOr(L) : List.of(L);
           List<Node> rightTerms = R instanceof Or ? flattenOr(R) : List.of(R);
           Node result = null;
                for (Node lt : leftTerms) {
                    for (Node rt : rightTerms) {
                    Node term = simplifyAnd(lt, rt);
                        if (result == null) result = term;
                        else result = new Or(result, term);
                  }
               }
            tracer("R6/R7", "Distributive applied: " + L + " * " + R + " → " + result);
            return simplify(result);
           }
       // return AND node as it is
        return new And(L,R);
      }



// Simplify OR with boolean algebra
static Node simplifyOr(Node L, Node R) {
        L = simplify(L);
        R = simplify(R);
        
        
        
        
   // Rule R1 : Idempotent Law (A * A = A)
   // AND of same term = same term
   if (L.toString().equals(R.toString())) {
       tracer("R1", "Idempotent Law: " + L + " + " + R + " → " + L);
       return L;
      }
          
     // Rule 2 - Null law (A*0 = 0) 
     // If 0 is present, the whole AND is 0
      if ((L instanceof Const && ((Const)L).value) || (R instanceof Const && ((Const)R).value)) {
         tracer("R2", "Null Law: OR with 1 → 1");
         return new Const(true);
        }


    // Rule 3 - Identity law (A*1 = A)
    // Multiplying by 1 results in same answer 
      if (L instanceof Const && !((Const)L).value) return R;
      if (R instanceof Const && !((Const)R).value) return L;

   
    // Rule 4 - Complement Law (A*A' = 0)
    // If one side is the negation of the other, result is 0
        if (isComplement(L,R)) {
        tracer("R4", "Complement Law: " + L + " + " + R + " → 1");
        return new Const(true);
        }

    // Rule R5: Absorption Law (A + AB = A)
    // Flatten AND to detect if OR has a repeated term    if (L instanceof And) {
       if (L instanceof And) {
        for (Node f : flattenAnd((And)L)) {
             if (f.toString().equals(R.toString())) {
                 tracer("R5", "Absorption: (" + L + ") + " + R + " → " + R);
                       return R;
                     }
               }
     }
     if (R instanceof And) {
        for (Node f : flattenAnd (R)) {
             if (f.toString().equals(L.toString())) {
                 tracer("R5", "Absorption: " + L + " + (" + R + ") → " + L);
                 return L;
                   }
           }
          }
       // return OR node as is
        return new Or(L,R);
         }
// ======== DRIVER ========
public static void main(String[] args) {
Scanner in = new Scanner(System.in);
System.out.print("Enter Boolean Expression: ");
String expr = in.nextLine();
Parser p = new Parser(expr);
Node root = p.parse();
System.out.println("Parsed: " + root);
Node simplified = NoSimplification(root);     
System.out.println("Simplified: " + simplified);
System.out.println("\nNOTE: This is only the starter.");
System.out.println("Students must finish all rule logic in simplify().");
}
}
