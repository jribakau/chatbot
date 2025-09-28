import { ResourceStatusEnum } from "../enums/resourceStatusEnum";

export interface Resource {
    id?: string;
    ownerId?: string;
    resourceStatus?: ResourceStatusEnum;
    createdAt?: Date;
    updatedAt?: Date;
}
