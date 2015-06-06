package net.thebix.debts.enums;

//Виды сортировки листов должников
public final class DebitorsListSortTypes {
		private DebitorsListSortTypes(){}

        public static final int None = 0;
		public static final int Alphabetically = 1;
		public static final int AlphabeticallyDesc = 2;
		public static final int Date = 3;
        public static final int DateDesc = 4;
        public static final int Amount = 5;
        public static final int AmountDesc = 6;
}
