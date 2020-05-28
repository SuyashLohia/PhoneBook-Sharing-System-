// Passwords of users "admin" and "kpchow" are "admin99" and "default" respectively. 
public class AccessControlManager {
	User[] userlist = new User[1000];
	int n = 0;
	
	public AccessControlManager() {
		userlist[0] = new User("admin","admin99",true,true); //Password of admin is admin99
		userlist[1] = new User("kpchow","default",true,true); // Password of kpchow is default 
		userlist[2] = new User("3035550406","default",true,false); //New user added with username as my HKU ID
		
		n = 3;
	}

	// checkUser: check if user �nm� already exists in the userlist
	//	if nm is found, returns the index to the name in userlist
	//	otherwise, returns �1
	public int checkUser(String nm)
	{
		for (int i=0; i<n; ++i)
		{
			if ( nm.equals(userlist[i].username) ) return i;
		}
		return -1;
	}

	// verifyPassword: verify the password of user �nm� is �pwd�
	public boolean verifyPassword(String nm, String pwd)
	{
		int idx = checkUser(nm);
		if ( idx >= 0 )
		{
			if ( pwd.equals(userlist[idx].password)) return true;
		}
		return false;
	}

	// checkReadPerm: check if user �nm� has the read permission,
	//			 i.e. read_perm is true
	public boolean checkReadPerm(String nm)
	{
		int idx = checkUser(nm);
		if ( idx >= 0 )
		{
			return (userlist[idx].read_perm);
		}
		return false;
	}

	// checkWritePerm: check if user �nm� has the write permission,
	//			 i.e. write_perm is true
	public boolean checkWritePerm(String nm)
	{
		int idx = checkUser(nm);
		if ( idx >= 0 )
		{
			return (userlist[idx].write_perm);
		}
		return false;
	}

}
