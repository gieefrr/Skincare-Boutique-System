package data;

import java.util.ArrayList;
import model.Product;

public class SortSearch {   // all static so we never need to make an object

    public static void bubbleSortByPrice(ArrayList<Product> list) {   // cheapest first
        int n = list.size();
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - 1 - i; j++) {
                if (list.get(j).getPrice() > list.get(j + 1).getPrice()) {
                    Product temp = list.get(j);
                    list.set(j, list.get(j + 1));
                    list.set(j + 1, temp);
                }
            }
        }
    }
    public static void selectionSortByName(ArrayList<Product> list) {   // A to Z
        int n = list.size();
        for (int i = 0; i < n - 1; i++) {
            int minIndex = i;
            for (int j = i + 1; j < n; j++) {
                if (list.get(j).getName().compareToIgnoreCase(list.get(minIndex).getName()) < 0) {
                    minIndex = j;
                }
            }
            Product temp = list.get(i);
            list.set(i, list.get(minIndex));
            list.set(minIndex, temp);
        }
    }
    public static void insertionSortById(ArrayList<Product> list) {   // lowest ID first
        for (int i = 1; i < list.size(); i++) {
            Product key = list.get(i);
            int j = i - 1;
            while (j >= 0 && list.get(j).getId() > key.getId()) {
                list.set(j + 1, list.get(j));
                j--;
            }
            list.set(j + 1, key);
        }
    }
    public static ArrayList<Product> linearSearch(ArrayList<Product> list, String keyword) {   // checks the name, brand and category
        ArrayList<Product> results = new ArrayList<>();
        String key = keyword.toLowerCase();
        for (int i = 0; i < list.size(); i++) {
            Product product = list.get(i);
            if (product.getName().toLowerCase().contains(key)
                    || product.getBrand().toLowerCase().contains(key)
                    || product.getCategory().toLowerCase().contains(key)) {
                results.add(product);
            }
        }
        return results;
    }
    public static Product binarySearchById(ArrayList<Product> sortedList, int id) {   // list has to be sorted by ID first
        int low = 0;
        int high = sortedList.size() - 1;
        while (low <= high) {
            int mid = (low + high) / 2;
            int midId = sortedList.get(mid).getId();
            if (midId == id) {
                return sortedList.get(mid);
            } else if (midId < id) {
                low = mid + 1;      // search the right half
            } else {
                high = mid - 1;     // search the left half
            }
        }
        return null;
    }
}
