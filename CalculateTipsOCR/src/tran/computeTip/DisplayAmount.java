package tran.computeTip;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * A class to determine how much to pay after a tip has been computed.
 * @author Todd
 */
public class DisplayAmount {

	/**
	 * Constructor to access this class
	 */
	public DisplayAmount() {}
	
	/**
	 * @param s_StringToParse The unparsed string with the subtotal to be paid
	 * @param i_NumberOfPayers The amount of people paying the bill
	 * @param d_tipPercentage The amount to be tipped as a percentage
	 * @return A string with the amount to pay
	 */
	public String practiceParser(String s_StringToParse, int i_NumberOfPayers, double d_tipPercentage) {
		String s_firstReceiptPattern = "^\\w{5} \\d{2}\\.\\d{2}$";
		
		String[] sa_parsedStringContents = s_StringToParse.split("\n");
		
		String s_totalBeforeTip = "";
		double d_TheAmountToAppend = 0;
		double d_newTotal = 0;
		
		for(int i = 0; i < sa_parsedStringContents.length; i++) {
			if(sa_parsedStringContents[i].matches(s_firstReceiptPattern) /* || sa_parsedStringContents[i].matches(s_secondReceiptPattern) */) {
				String[] sa_tempArray = sa_parsedStringContents[i].split(" ");
				s_totalBeforeTip = sa_tempArray[1];
				d_TheAmountToAppend = Double.parseDouble(s_totalBeforeTip);
				break;
			}
		}
		
		if(s_totalBeforeTip.length() == 0)
			return "Sorry, you must re-take the picture and make sure it is clearer.";
		BigDecimal bd_roundTip = new BigDecimal(d_TheAmountToAppend * (d_tipPercentage / 100));
		bd_roundTip = bd_roundTip.setScale(2, RoundingMode.CEILING);
		
		BigDecimal bd_totalToAddTip = new BigDecimal(Double.parseDouble(s_totalBeforeTip));
		d_newTotal = (bd_totalToAddTip.add(bd_roundTip)).doubleValue();
		
		if(i_NumberOfPayers > 1) {
			BigDecimal bd_newTotal = new BigDecimal(d_newTotal / i_NumberOfPayers);
			bd_newTotal = bd_newTotal.setScale(2, RoundingMode.CEILING);
			System.out.println("Each person will pay: $" + String.valueOf(bd_newTotal));
			return "Each person will pay: $" + String.valueOf(bd_newTotal);
		}
		else {
			System.out.println("You will pay: $" + String.valueOf(d_newTotal));
			return "You will pay: $" + String.valueOf(d_newTotal);
		}
	}
}
