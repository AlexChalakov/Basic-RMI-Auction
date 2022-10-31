import javax.crypto.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.*;
import java.util.Arrays;

public class Server implements Auction{

    protected AuctionItem[] items;
    protected AuctionItem item;

    protected SecretKey aesKey;

    protected Cipher cipher;

    protected SealedObject sealedObject;

    public Server() throws NoSuchAlgorithmException {
        super();
        items = new AuctionItem[2];

        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        SecureRandom secureRandom = new SecureRandom();
        keyGenerator.init(secureRandom);
        aesKey = keyGenerator.generateKey();
        System.out.println("Aes key is " + aesKey);

        byte[] bytes = aesKey.getEncoded();
        try (FileOutputStream fos = new FileOutputStream("../keys/testKey.aes")) {
            fos.write(bytes);
            System.out.println("Writing done, bytes = " + Arrays.toString(bytes));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        item = new AuctionItem();
        item.itemID = 1;
        item.name = "Bike";
        item.description = "Has two wheels";
        item.highestBid = 100;
    }

    public static void main(String[] args) {
        try {
            Server s = new Server();
            String name = "Auction";
            Auction stub = (Auction) UnicastRemoteObject.exportObject(s, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(name, stub);
            System.out.println("Server ready");
        } catch (Exception e) {
            System.err.println("Exception:");
            e.printStackTrace();
        }
    }

    @Override
    public SealedObject getSpec(int itemID) throws RemoteException {
        items[0] = item;

        try {
            cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);
            sealedObject = new SealedObject(items[itemID], cipher);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IOException |
                 IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        }

        return sealedObject;
    }
}