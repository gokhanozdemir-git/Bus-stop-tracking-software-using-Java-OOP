package moovit;

import org.json.JSONObject;

public class Transfer {
    private String transferStopId;
    private int transferSure;
    private double transferUcret;

    public String getTransferStopId() { return transferStopId; }
    public void setTransferStopId(String transferStopId) { this.transferStopId = transferStopId; }

    public int getTransferSure() { return transferSure; }
    public void setTransferSure(int transferSure) { this.transferSure = transferSure; }

    public double getTransferUcret() { return transferUcret; }
    public void setTransferUcret(double transferUcret) { this.transferUcret = transferUcret; }
    
    public JSONObject toJSON() {
        JSONObject jsonTransfer = new JSONObject();
        jsonTransfer.put("transferStopId", this.transferStopId);
        jsonTransfer.put("transferSure", this.transferSure);
        jsonTransfer.put("transferUcret", this.transferUcret);
        return jsonTransfer;
        }

    @Override
    public String toString() {
        return "Transfer{ transferStopId='" + transferStopId + "', sure=" + transferSure + " dk, ucret=" + transferUcret + " TL }";
    }
}
