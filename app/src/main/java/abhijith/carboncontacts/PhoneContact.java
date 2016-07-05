package abhijith.carboncontacts;

public class PhoneContact implements Comparable<PhoneContact> {

    private String contactNumber;
    private String contactName;
    private String contactType;
    private String contactID;

    PhoneContact(String number, String name, String type, String id){
        contactNumber = number.trim().replaceAll(" ","");
        contactName = name;
        contactType = type;
        contactID = id;
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
    String getContactID(){
        return contactID;
    }

    @Override
    public int compareTo(PhoneContact another) {
        return new String(this.getContactNumber()).compareTo(new String(another.getContactNumber()));
    }
}
