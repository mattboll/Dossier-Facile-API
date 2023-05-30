package fr.dossierfacile.common.entity;

import fr.dossierfacile.common.enums.UserType;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "bo_user")
@DiscriminatorValue("BO")
@NoArgsConstructor
@SuperBuilder(builderMethodName = "lombokBuilder")
public class BOUser extends User implements Serializable {

    public static BOUser.BOUserBuilder<?, ?> builder() {
        BOUser.BOUserBuilder<?, ?> builder = BOUser.lombokBuilder();
        builder.userType(UserType.BO);
        return builder;
    }
}
