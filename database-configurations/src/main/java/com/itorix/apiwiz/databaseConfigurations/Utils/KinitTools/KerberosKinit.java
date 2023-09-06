package com.itorix.apiwiz.databaseConfigurations.Utils.KinitTools;

import sun.security.krb5.*;
import sun.security.krb5.internal.HostAddresses;
import sun.security.krb5.internal.KDCOptions;
import sun.security.krb5.internal.ccache.CredentialsCache;
import sun.security.util.Password;

import javax.security.auth.kerberos.KeyTab;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class KerberosKinit {

    private KerberosKinitOptions options;
    private static final boolean DEBUG = true;

    public static void main(String[] args) {
        try {
            KerberosKinit self = new KerberosKinit(args);
        }
        catch (Exception e) {
            String msg = null;
            if (e instanceof KrbException) {
                msg = ((KrbException)e).krbErrorMessage() + " " +
                    ((KrbException)e).returnCodeMessage();
            } else  {
                msg = e.getMessage();
            }
            if (msg != null) {
                System.err.println("Exception: " + msg);
            } else {
                System.out.println("Exception: " + e);
            }
            e.printStackTrace();
        }
        return;
    }

    public KerberosKinit(String[] args) throws IOException, KrbException {
        if (args == null || args.length == 0) {
            options = new KerberosKinitOptions();
        } else {
            options = new KerberosKinitOptions(args);
        }
        String princName = null;;
        PrincipalName principal = options.getPrincipal();
        if (principal != null) {
            princName = principal.toString();
        }
        KrbAsReqBuilder builder;
        if (DEBUG) {
            System.out.println("Principal is " + principal);
        }
        char[] psswd = options.password;
        boolean useKeytab = options.useKeytabFile();
        if (!useKeytab) {
            if (princName == null) {
                throw new IllegalArgumentException
                    (" Can not obtain principal name");
            }
            if (psswd == null) {
                System.out.print("Password for " + princName + ":");
                System.out.flush();
                psswd = Password.readPassword(System.in);
                if (DEBUG) {
                    System.out.println(">>> Kinit console input " +
                        new String(psswd));
                }
            }
            builder = new KrbAsReqBuilder(principal, psswd);
        } else {
            if (DEBUG) {
                System.out.println(">>> Kinit using keytab");
            }
            if (princName == null) {
                throw new IllegalArgumentException
                    ("Principal name must be specified.");
            }
            String ktabName = options.keytabFileName();
            if (ktabName != null) {
                if (DEBUG) {
                    System.out.println(
                                       ">>> Kinit keytab file name: " + ktabName);
                }
            }

            builder = new KrbAsReqBuilder(principal, ktabName == null
                    ? KeyTab.getInstance()
                    : KeyTab.getInstance(new File(ktabName)));
        }

        KDCOptions opt = new KDCOptions();
        setOptions(KDCOptions.FORWARDABLE, options.forwardable, opt);
        setOptions(KDCOptions.PROXIABLE, options.proxiable, opt);
        builder.setOptions(opt);
        String realm = options.getKDCRealm();
        if (realm == null) {
            realm = Config.getInstance().getDefaultRealm();
        }

        if (DEBUG) {
            System.out.println(">>> Kinit realm name is " + realm);
        }

        PrincipalName sname = PrincipalName.tgsService(realm, realm);
        builder.setTarget(sname);

        if (DEBUG) {
            System.out.println(">>> Creating KrbAsReq");
        }

        if (options.getAddressOption())
            builder.setAddresses(HostAddresses.getLocalAddresses());

        builder.action();

        sun.security.krb5.internal.ccache.Credentials credentials =
            builder.getCCreds();
        builder.destroy();

        // we always create a new cache and store the ticket we get
        CredentialsCache cache =
            CredentialsCache.create(principal, options.cachename);
        if (cache == null) {
           throw new IOException("Unable to create the cache file " +
                                 options.cachename);
        }
        cache.update(credentials);
        cache.save();

        if (options.password == null) {
            // Assume we're running interactively
            System.out.println("New ticket is stored in cache file " +
                               options.cachename);
         } else {
             Arrays.fill(options.password, '0');
         }

        // clear the password
        if (psswd != null) {
            Arrays.fill(psswd, '0');
        }
        options = null; // release reference to options
    }

    private static void setOptions(int flag, int option, KDCOptions opt) {
        switch (option) {
        case 0:
            break;
        case -1:
            opt.set(flag, false);
            break;
        case 1:
            opt.set(flag, true);
        }
    }
}
