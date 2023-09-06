package com.itorix.apiwiz.databaseConfigurations.Utils.KinitTools;

import sun.security.krb5.KrbException;
import sun.security.krb5.PrincipalName;
import sun.security.krb5.RealmException;
import sun.security.krb5.internal.KerberosTime;
import sun.security.krb5.internal.ccache.CCacheInputStream;
import sun.security.krb5.internal.ccache.FileCCacheConstants;
import sun.security.krb5.internal.ccache.FileCredentialsCache;

import java.io.FileInputStream;
import java.io.IOException;

class KerberosKinitOptions {
    public boolean validate = false;
    public short forwardable = -1;
    public short proxiable = 1;
    public boolean renew = false;
    public KerberosTime lifetime;
    public KerberosTime renewable_lifetime;
    public String target_service;
    public String keytab_file;
    public String cachename;
    private PrincipalName principal;
    public String realm;
    char[] password = null;
    private boolean DEBUG = true;
    private boolean includeAddresses = true; // default.
    private boolean useKeytab = false; // default = false.
    private String ktabName; // keytab file name

    public KerberosKinitOptions() throws RuntimeException, RealmException {
        // no args were specified in the command line;
        // use default values
        cachename = FileCredentialsCache.getDefaultCacheName();
        if (cachename == null) {
            throw new RuntimeException("default cache name error");
        }
        principal = getDefaultPrincipal();
    }


    public String getKDCRealm() {
        if (realm == null) {
            if (principal != null) {
                return principal.getRealmString();
            }
        }
        return null;
    }

    public KerberosKinitOptions(String[] args)
        throws KrbException, RuntimeException, IOException {
        // currently we provide support for -f -p -c principal options
        String p = null; // store principal

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-f")) {
                forwardable = 1;
            } else if (args[i].equals("-p")) {
                proxiable = 1;
            } else if (args[i].equals("-c")) {

                if (args[i + 1].startsWith("-")) {
                    throw new IllegalArgumentException("input format " +
                                                       " not correct: " +
                                                       " -c  option " +
                                                       "must be followed " +
                                                       "by the cache name");
                }
                cachename = args[++i];
                if ((cachename.length() >= 5) &&
                    cachename.substring(0, 5).equalsIgnoreCase("FILE:")) {
                    cachename = cachename.substring(5);
                };
            } else if (args[i].equals("-A")) {
                includeAddresses = false;
            } else if (args[i].equals("-k")) {
                useKeytab = true;
            } else if (args[i].equals("-t")) {
                if (ktabName != null) {
                    throw new IllegalArgumentException
                        ("-t option/keytab file name repeated");
                } else if (i + 1 < args.length) {
                    ktabName = args[++i];
                } else {
                    throw new IllegalArgumentException
                        ("-t option requires keytab file name");
                }

                useKeytab = true;
            } else if (p == null) { // Haven't yet processed a "principal"
                p = args[i];
                try {
                    principal = new PrincipalName(p);
                } catch (Exception e) {
                    throw new IllegalArgumentException("invalid " +
                                                       "Principal name: " + p +
                                                       e.getMessage());
                }
            } else if (this.password == null) {
                // Have already processed a Principal, this must be a password
                password = args[i].toCharArray();
            } else {
                throw new IllegalArgumentException("too many parameters");
            }
        }
        // we should get cache name before getting the default principal name
        if (cachename == null) {
            cachename = FileCredentialsCache.getDefaultCacheName();
            if (cachename == null) {
                throw new RuntimeException("default cache name error");
            }
        }
        if (principal == null) {
            principal = getDefaultPrincipal();
        }
    }

    PrincipalName getDefaultPrincipal() {

        // get default principal name from the cachename if it is
        // available.

        try {
            CCacheInputStream cis =
                new CCacheInputStream(new FileInputStream(cachename));
            int version;
            if ((version = cis.readVersion()) ==
                    FileCCacheConstants.KRB5_FCC_FVNO_4) {
                cis.readTag();
            } else {
                if (version == FileCCacheConstants.KRB5_FCC_FVNO_1 ||
                        version == FileCCacheConstants.KRB5_FCC_FVNO_2) {
                    cis.setNativeByteOrder();
                }
            }
            PrincipalName p = cis.readPrincipal(version);
            cis.close();
            if (DEBUG) {
                System.out.println(">>>KinitOptions principal name from "+
                                   "the cache is :" + p);
            }
            return p;
        } catch (IOException e) {
            // ignore any exceptions; we will use the user name as the
            // principal name
            if (DEBUG) {
                e.printStackTrace();
            }
        } catch (RealmException e) {
            if (DEBUG) {
                e.printStackTrace();
            }
        }

        String username = System.getProperty("user.name");
        if (DEBUG) {
            System.out.println(">>>KinitOptions default username is :"
                               + username);
        }
        try {
            PrincipalName p = new PrincipalName(username, "example.com");
            return p;
        } catch (RealmException e) {
            // ignore exception , return null
            if (DEBUG) {
                System.out.println ("Exception in getting principal " +
                                    "name " + e.getMessage());
                e.printStackTrace();
            }
        }
        return null;
    }

    public boolean getAddressOption() {
        return includeAddresses;
    }

    public boolean useKeytabFile() {
        return useKeytab;
    }

    public String keytabFileName() {
        return ktabName;
    }

    public PrincipalName getPrincipal() {
        return principal;
    }
}
