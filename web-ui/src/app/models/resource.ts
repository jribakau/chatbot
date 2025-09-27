import { ResourceStatusEnum } from "../enums/resourceStatusEnum";

export interface Resource {
    id?: string;
    resourceStatus?: ResourceStatusEnum;
    createdAt?: Date;
    updatedAt?: Date;
}
