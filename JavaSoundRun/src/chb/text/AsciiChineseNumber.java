/**
 * 
 */
package chb.text;

public class AsciiChineseNumber {

	/**
	 * 测试程序的可行性
	 * 
	 * @param args
	 */
	public static void test() {
		System.out.println("\n--------将数字转换成中文金额的大写形式------------\n");

		String s = AsciiChineseNumber.cleanZero(AsciiChineseNumber
				.splitNum("86434"));
		// 如果转换过后是一个空串，则不输出屏幕
		if (!"".equals(s)) {
			System.out.println("转换成中文后为：" + s);
			;
		}
		System.out.println("\n---------------------------------------------");
	}

	/**
	 * 把用户输入的数以小数点为界分割开来，并调用 numFormat() 方法 进行相应的中文金额大写形式的转换 注：传入的这个数应该是经过
	 * roundString() 方法进行了四舍五入操作的
	 * 
	 * @param s
	 *            String
	 * @return 转换好的中文金额大写形式的字符串
	 */
	public static String splitNum(String s) {

		if ("".equals(s)) {
			return "";
		}
		int index = s.indexOf(".");
		if (index >= 0) {
			String intOnly = s.substring(0, index);
			String part1 = numFormat(1, intOnly);

			String smallOnly = s.substring(index + 1);
			String part2 = numFormat(2, smallOnly);

			if (part2.length() > 0)
				part2 = "点" + part2;
			String newS = part1 + part2;
			return newS;
		} else {
			String newS = numFormat(1, s);
			return newS;
		}
	}

	/**
	 * 把传入的数转换为中文金额大写形式
	 * 
	 * @param flag
	 *            int 标志位，1 表示转换整数部分，0 表示转换小数部分
	 * @param s
	 *            String 要转换的字符串
	 * @return 转换好的带单位的中文金额大写形式
	 */
	public static String numFormat(int flag, String s) {
		int sLength = s.length();
		// 货币大写形式
		String bigLetter[] = { "零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖" };
		// 货币单位
		String unit[] = { "", "拾", "佰", "仟", "万",
				// 拾万位到仟万位
				"拾", "佰", "仟",
				// 亿位到万亿位
				"亿", "拾", "佰", "仟", "万" };

		String newS = "";

		for (int i = 0; i < sLength; i++) {
			if (flag == 1) {
				newS = newS + bigLetter[s.charAt(i) - 48]
						+ unit[sLength - i - 1];
			} else if (flag == 2) {
				newS = newS + bigLetter[s.charAt(i) - 48];
			}
		}
		return newS;
	}

	/**
	 * 把已经转换好的中文金额大写形式加以改进，清理这个字 符串里面多余的零，让这个字符串变得更加可观 注：传入的这个数应该是经过 splitNum()
	 * 方法进行处理，这个字 符串应该已经是用中文金额大写形式表示的
	 * 
	 * @param s
	 *            String 已经转换好的字符串
	 * @return 改进后的字符串
	 */
	public static String cleanZero(String s) {

		if ("".equals(s)) {
			return "";
		}

		while (s.charAt(0) == '零') {
			s = s.substring(2);
			if (s.length() == 0) {
				return "零";
			}
		}

		String regex1[] = { "零仟", "零佰", "零拾" };
		String regex2[] = { "零亿", "零万", "零元" };
		String regex3[] = { "亿", "万", "元" };
		String regex4[] = { "零角", "零分" };

		for (int i = 0; i < 3; i++) {
			s = s.replaceAll(regex1[i], "零");
		}

		for (int i = 0; i < 3; i++) {
			s = s.replaceAll("零零零", "零");
			s = s.replaceAll("零零", "零");
			s = s.replaceAll(regex2[i], regex3[i]);
		}

		for (int i = 0; i < 2; i++) {
			s = s.replaceAll(regex4[i], "");
		}
		 if (s.charAt(s.length()-1) == '零')
			 s = s.substring(0, s.length()-1);

		s = s.replaceAll("亿万", "亿");
		return s;
	}
}