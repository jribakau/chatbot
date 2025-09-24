import { UUID } from "crypto";
import { ResourceStatusEnum } from "../enums/resourceStatusEnum";

export interface Resource {
    id?: UUID;
    resourceStatus?: ResourceStatusEnum;
    createdAt?: Date;
    updatedAt?: Date;
}
