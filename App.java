import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class App {
	static public class ValueNode implements INode {
		private int value;

		public ValueNode(int value) {
			this.value = value;
		}

		public TreeMap<Long, Float> Calculate() {
			TreeMap<Long, Float> result = new TreeMap<Long, Float>();
			result.put((long)value, 1.0f);
			return result;
		}
	}

	static public class DiceNode implements INode {
		private int faceCount;

		public DiceNode(int faceCount) {
			this.faceCount = faceCount;
		}

		public TreeMap<Long, Float> Calculate() {
			TreeMap<Long, Float> result = new TreeMap<Long, Float>();
			
			float prohibitance = 1.0f / this.faceCount;
			for(int i = 1; i <= faceCount; i++){
				result.put((long)i, prohibitance);
			}

			return result;
		}
		
	}

	static public class OperatorNode implements INode {
		private char operator;
		private INode leftNode;
		private INode rightNode;

		public OperatorNode(char operator) {
			this.operator = operator;
		}

		public void SetLeftNode(INode leftNode) {
			this.leftNode = leftNode;
		}

		public void SetRightNode(INode rightNode) {
			this.rightNode = rightNode;
		}

		
		public TreeMap<Long, Float> Calculate() {
			TreeMap<Long, Float> leftResult = this.leftNode.Calculate();
			TreeMap<Long, Float> rightResult = this.rightNode.Calculate();
			TreeMap<Long, Float> result = new TreeMap<Long, Float>();
			
			for (Map.Entry<Long, Float> lr : leftResult.entrySet()) {
				for (Map.Entry<Long, Float> rh : rightResult.entrySet()) {
					Long newValue = 0l;

					if (operator == '+'){
						newValue = lr.getKey() + rh.getKey();
					} else if (operator == '*'){
						newValue = lr.getKey() * rh.getKey();
					}  else if (operator == '-'){
						newValue = lr.getKey() - rh.getKey();
					} else if (operator == '>') {
						if (lr.getKey() > rh.getKey()){
							newValue = 1l;
						}
					}
					
					float prohibitance = lr.getValue() * rh.getValue();

					if (!result.containsKey(newValue)){
						result.put(newValue, prohibitance);
					} else {
						result.put(newValue, result.get(newValue) + prohibitance);
					}
				}
			}
			
			return result;
		}
	}

	static public interface INode {
		public TreeMap<Long, Float> Calculate();
	}

	public static void main(String[] args) {
		java.util.Scanner in = new java.util.Scanner(System.in);
		String line = in.nextLine();
		INode node = ParseExpression(line);
		TreeMap<Long, Float> result = node.Calculate();

		for (Map.Entry<Long, Float> m : result.entrySet()) {
			System.out.println(m.getKey() + " " + String.format(Locale.US, "%.2f", (float)Math.round(m.getValue()*100*100)/100));
		}
	}

    private static INode ParseExpression(String expresion){
		CharacterIterator it = new StringCharacterIterator(expresion);

		return CompareExpr(it);
	}

	private static INode CompareExpr(CharacterIterator it){
		if (GetToken(it) == CharacterIterator.DONE){
			return null;
		}

		INode left = PlusMinusExpr(it);
		while (GetToken(it) != CharacterIterator.DONE){
			if (GetToken(it) == '>') {
				it.next();

				var operation = new OperatorNode('>');
				INode right = PlusMinusExpr(it);
				operation.SetLeftNode(left);
				operation.SetRightNode(right);
				left = operation;
			} else {
				return left;
			}
		}

		return left;
	}

	private static INode PlusMinusExpr(CharacterIterator it){
		if (GetToken(it) == CharacterIterator.DONE){
			return null;
		}

		INode left = MultipleExpr(it);
		while (GetToken(it) != CharacterIterator.DONE){
			if (GetToken(it) == '+' || GetToken(it) == '-') {
				var c = GetToken(it);
				it.next();

				var operation = new OperatorNode(c);
				INode right = MultipleExpr(it);
				operation.SetLeftNode(left);
				operation.SetRightNode(right);
				left = operation;
			} else {
				return left;
			}
		}

		return left;
	}

	private static INode MultipleExpr(CharacterIterator it) {
		if (GetToken(it) == CharacterIterator.DONE){
			return null;
		}

		INode left = PrimaryValue(it);

		while (GetToken(it) != CharacterIterator.DONE){
			if (GetToken(it) == '*') {
				var c = GetToken(it);
				it.next();
	
				var operation = new OperatorNode(c);
				INode right = PrimaryValue(it);
				operation.SetLeftNode(left);
				operation.SetRightNode(right);
				left = operation;
			}
			else{
				return left;
			}
		}

		return left;
	}

	private static INode PrimaryValue(CharacterIterator it) {
		if (GetToken(it) == CharacterIterator.DONE){
			return null;
		}

		if (Character.isDigit(GetToken(it))) {
			int number = GetNumber(it);
			return new ValueNode(number);
		} 
		
		if (it.current() == 'd') {
			it.next();
			int number = GetNumber(it);
			return new DiceNode(number);
		} 
		
		if (GetToken(it) == '(') {
			it.next();
			var node = CompareExpr(it);
			if (GetToken(it) == ')'){
				it.next();
			}

			return node;
		}

		return null;
	}

	private static int GetNumber(CharacterIterator it){
		String strNumber = "";
		while (Character.isDigit(it.current())){
			strNumber += it.current();
			it.next();
		}

		return Integer.parseInt(strNumber);
	}

	private static char GetToken(CharacterIterator it) {
		if (it.current() == ' '){
			it.next();
			return GetToken(it);
		}

		return it.current();
	}
}
