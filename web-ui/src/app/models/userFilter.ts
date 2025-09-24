import { ResourceStatusEnum } from "../enums/resourceStatusEnum";

export interface UserFilter {
    email?: string;
    username?: string;
    resourceStatus?: ResourceStatusEnum
}