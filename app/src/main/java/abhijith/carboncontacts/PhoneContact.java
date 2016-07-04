package abhijith.carboncontacts;

/**
 * Created by Abhijith on 04-07-2016.
 */
public class PhoneContact implements Comparable<PhoneContact> {

    private String contactNumber;
    private String contactName;
    private String contactType;

    PhoneContact(String number, String name, String type){
        contactNumber = number;
        contactName = name;
        contactType = type;
    }

    String getContactNumber(){
        return contactNumber;
    }
    String getContactName(){
        return contactName;
    }
    String getContactType(){
        return contactType;
    }

    @Override
    public int compareTo(PhoneContact another) {
        return new String(this.getContactNumber()).compareTo(new String(another.getContactNumber()));
    }
}
