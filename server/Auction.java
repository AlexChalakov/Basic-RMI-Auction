import javax.crypto.SealedObject;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Auction extends Remote {
    public SealedObject getSpec(int itemID) throws RemoteException;
}

