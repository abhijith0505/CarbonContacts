package abhijith.carboncontacts;

/*
    Object of this class depicts a single phone number.
    Each phone number within a contact is considered as a different object
*/


public class PhoneContact implements Comparable<PhoneContact> {

    private String contactNumber;
    private String contactName;
    private String contactType;
    private String contactID;
    private String contactNumberID;

    //TODO: Add more fields like display pic, email etc for better user experience

    PhoneContact(String number, String name, String type, String id, String phoneNumberID){
        contactNumber = number.trim().replaceAll(" ","");
        contactName = name;
        contactType = type;
        contactID = id;
        contactNumberID = phoneNumberID;
    }

    /*Getters for each phone contact field*/

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
    String getContactNumberID(){
        return contactNumberID;
    }

    //Comparator to sort the PhoneContact objects according to the number
    @Override
    public int compareTo(PhoneContact another) {
        return new String(this.getContactNumber()).compareTo(new String(another.getContactNumber()));
    }
}
