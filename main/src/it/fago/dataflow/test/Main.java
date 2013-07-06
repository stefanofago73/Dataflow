package it.fago.dataflow.test;

import it.fago.dataflow.DataFlowVariable;

import java.security.SecureRandom;

/**
 * 
 * @author Stefano Fago
 * 
 */
public class Main {

	public static void main(String[] args) {
		final CoffeMachine cm = new CoffeMachine();

		Thread t = new Thread() {
			public void run() {
				cm.init();
				cm.service();
			}
		};
		t.start();

		while (true) {
			User user = new User(cm);
			user.chooseABeverage();
			user.payForBeverage();
			user.obtainBeverage();
		}
	}

	// ==========================================================
	//
	// ==========================================================

	public static class CoffeMachine {

		public enum Beverage {
			Coffee("COFFEE", 1), Tea("TEA", 2), Milk("MILK", 3), Chocolate(
					"CHOCO", 4);

			private int fee;
			private String description;

			private Beverage(String description, int fee) {
				this.description = description;
				this.fee = fee;
			}

			public int getFee() {
				return fee;
			}

			public String getDescription() {
				return description;
			}
		}// END

		DataFlowVariable<Beverage> selectDrink = new DataFlowVariable<Beverage>();
		DataFlowVariable<Boolean> beverageReady = new DataFlowVariable<Boolean>();
		DataFlowVariable<Integer> coin = new DataFlowVariable<Integer>();
		DataFlowVariable<Boolean> canPutCoin = new DataFlowVariable<Boolean>();
		private int actuallyPaid;

		public void init() {
			cleanWaterPipe();
			warmUp();
			loadAllBeverage();
		}

		public void service() {
			while (true) {
				showDrinksMenu();
				Beverage beverage = waitForDrinkChoosen();
				waitForAllCoinsIn(beverage);
				prepareAndDistributeDrink();
			}
		}

		// ######################################################
		// USER INTERFACE
		// ######################################################

		public void chooseBeverage(Beverage beverage) {
			if (beverage != null) {
				selectDrink.set(beverage);
			}
		}

		public void insertCoins(int coin) {
			if (canPutCoin.get()) {
				this.coin.set(coin);
			}
		}

		public Beverage obtainBeverage() {
			beverageReady.get();
			Beverage tmp = selectDrink.get();
			canPutCoin = new DataFlowVariable<Boolean>();
			beverageReady = new DataFlowVariable<Boolean>();
			selectDrink = new DataFlowVariable<Beverage>();
			actuallyPaid = 0;
			return tmp;
		}

		// ######################################################
		//
		// ######################################################

		protected void cleanWaterPipe() {
		}

		protected void warmUp() {

		}

		protected void loadAllBeverage() {

		}

		protected void prepareAndDistributeDrink() {
			System.out.println("## preparing beverage...");
			WorkersUtil.pause(4000L);
			System.out.println("## beverage ready!");
			beverageReady.set(true);
			WorkersUtil.pause(2000L);
		}

		protected void waitForAllCoinsIn(Beverage beverage) {
			while (actuallyPaid < beverage.getFee()) {
				System.out.println("## insert coin...");
				canPutCoin.set(true);
				actuallyPaid += coin.get();
				canPutCoin = new DataFlowVariable<Boolean>();
				coin = new DataFlowVariable<Integer>();
			}
		}

		protected Beverage waitForDrinkChoosen() {
			Beverage beverage = selectDrink.get();
			System.out.println("## choosen " + beverage.getDescription()
					+ " FEE: " + beverage.getFee());
			return beverage;
		}

		protected void showDrinksMenu() {
			StringBuilder sb = new StringBuilder();
			sb.append("\n\n#############################").append(
					"\n## select drink...");
			Beverage[] bb = Beverage.values();
			for (int i = 0; i < bb.length; i++) {
				sb.append("\n## " + bb[i].getDescription() + " COINS: "
						+ bb[i].getFee());
			}
			sb.append("\n#############################\n\n");
			System.out.println(sb);
		}

	}// END

	public static class User {
		private CoffeMachine cm;
		private int coinNeeded;
		private int choose;

		public User(CoffeMachine cm) {
			this.cm = cm;
		}

		public void chooseABeverage() {
			System.err.println("\tUhm... A Coofee Machine... Let Me Choose...");
			WorkersUtil.pause(2000);
			CoffeMachine.Beverage bvs[] = CoffeMachine.Beverage.values();
			SecureRandom rnd = new SecureRandom();
			choose = rnd.nextInt(bvs.length);
			System.err.println("\tUhm... Ok...I'll take a "
					+ bvs[choose].getDescription());
			cm.chooseBeverage(bvs[choose]);
			WorkersUtil.pause(2000);
			coinNeeded = bvs[choose].getFee();
		}

		public void payForBeverage() {
			for (int shoot = 1; shoot <= coinNeeded; shoot++) {
				System.err.println("\tok " + shoot + " coin...");
				cm.insertCoins(1);
				WorkersUtil.pause(1000);
			}
		}

		public void obtainBeverage() {
			CoffeMachine.Beverage bvs[] = CoffeMachine.Beverage.values();
			System.err.println("\tUhm... I'm waiting for "
					+ bvs[choose].getDescription());
			cm.obtainBeverage();
			System.err.println("\tOk...Let's drink my "
					+ bvs[choose].getDescription());
			WorkersUtil.pause(4000);
		}

	}// END

}// END