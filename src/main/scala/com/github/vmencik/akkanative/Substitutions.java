package com.github.vmencik.akkanative;

import com.oracle.svm.core.annotate.Delete;
import com.oracle.svm.core.annotate.TargetClass;

@Delete
@TargetClass(className = "org.mariadb.jdbc.internal.com.send.authentication.SendPamAuthPacket")
final class Target_org_mariadb_jdbc_internal_com_send_authentication_SendPamAuthPacket {
}
